import hashlib
import io
import json
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch
import shared_resources

class SharedResourcesTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        stream = io.BytesIO()
        self.files = {'tools/example.py': b'print(1)\n'}
        with zipfile.ZipFile(stream, 'w') as archive:
            for name, data in self.files.items():
                archive.writestr(name, data)
        self.payload = stream.getvalue()
        self.config = {'version': '1.0.0', 'url': 'https://github.com/ikaros0202/SanXu/releases/download/tools-v1.0.0/fc3d-tools-1.0.0.zip', 'sha256': hashlib.sha256(self.payload).hexdigest(), 'files': {name: hashlib.sha256(data).hexdigest() for name, data in self.files.items()}}
        self.lock = self.root / 'shared-resources.json'
        self.lock.write_text(json.dumps(self.config))
        self.cache = self.root / 'cache'

    def resolve(self):
        return shared_resources.resolve(self.lock, self.cache)

    def test_download_then_offline_cache_hit(self):
        with patch('shared_resources.urlopen', return_value=io.BytesIO(self.payload)) as request:
            first = self.resolve()
            self.assertEqual((first / 'tools/example.py').read_bytes(), self.files['tools/example.py'])
            request.assert_called_once()
        with patch('shared_resources.urlopen', side_effect=AssertionError('network not allowed')):
            self.assertEqual(first, self.resolve())

    def test_wrong_archive_hash_leaves_no_installed_cache(self):
        with patch('shared_resources.urlopen', return_value=io.BytesIO(b'corrupt')):
            with self.assertRaisesRegex(ValueError, 'SHA-256'):
                self.resolve()
        self.assertFalse(any(path.is_dir() and not path.name.startswith('.') for path in self.cache.iterdir()))

    def test_modified_cache_is_rejected_without_overwriting(self):
        with patch('shared_resources.urlopen', return_value=io.BytesIO(self.payload)):
            installed = self.resolve()
        target = installed / 'tools/example.py'
        target.write_bytes(b'modified')
        with self.assertRaisesRegex(ValueError, 'cache'):
            self.resolve()
        self.assertEqual(target.read_bytes(), b'modified')

    def test_unexpected_archive_entry_is_rejected(self):
        stream = io.BytesIO()
        with zipfile.ZipFile(stream, 'w') as archive:
            archive.writestr('../outside', b'bad')
        payload = stream.getvalue()
        self.config['sha256'] = hashlib.sha256(payload).hexdigest()
        self.lock.write_text(json.dumps(self.config))
        with patch('shared_resources.urlopen', return_value=io.BytesIO(payload)):
            with self.assertRaisesRegex(ValueError, 'entries'):
                self.resolve()
        self.assertFalse((self.root / 'outside').exists())

    def test_wrong_file_hash_is_rejected(self):
        self.config['files']['tools/example.py'] = '0' * 64
        self.lock.write_text(json.dumps(self.config))
        with patch('shared_resources.urlopen', return_value=io.BytesIO(self.payload)):
            with self.assertRaisesRegex(ValueError, 'cache'):
                self.resolve()

    def test_cache_override_is_shared_between_projects(self):
        other_lock = self.root / 'other-project.json'
        other_lock.write_text(self.lock.read_text())
        with patch('shared_resources.urlopen', return_value=io.BytesIO(self.payload)):
            first = self.resolve()
        with patch('shared_resources.urlopen', side_effect=AssertionError('network not allowed')):
            self.assertEqual(first, shared_resources.resolve(other_lock, self.cache))

    def test_concurrent_installers_download_once(self):
        from concurrent.futures import ThreadPoolExecutor
        with patch('shared_resources.urlopen', return_value=io.BytesIO(self.payload)) as request:
            with ThreadPoolExecutor(max_workers=2) as pool:
                installed = list(pool.map(lambda _: self.resolve(), range(2)))
            self.assertEqual(installed[0], installed[1])
            request.assert_called_once()

    def test_non_release_url_rejected_before_download(self):
        self.config['url'] = 'https://example.com/tool.zip'
        self.lock.write_text(json.dumps(self.config))
        with patch('shared_resources.urlopen') as request:
            with self.assertRaisesRegex(ValueError, 'URL'):
                self.resolve()
            request.assert_not_called()

    def test_unsafe_manifest_path_is_rejected(self):
        self.config['files'] = {'../outside': '0' * 64}
        self.lock.write_text(json.dumps(self.config))
        with self.assertRaisesRegex(ValueError, 'Unsafe'):
            self.resolve()

    def test_transient_network_failure_is_retried(self):
        from urllib.error import URLError
        with patch('shared_resources.urlopen', side_effect=[URLError('temporary TLS failure'), io.BytesIO(self.payload)]) as request, patch('shared_resources.time.sleep'):
            installed = self.resolve()
            self.assertTrue(installed.is_dir())
            self.assertEqual(request.call_count, 2)

    def test_network_failure_does_not_leave_partial_installation(self):
        from urllib.error import URLError
        with patch('shared_resources.urlopen', side_effect=URLError('offline')), patch('shared_resources.time.sleep'):
            with self.assertRaises(URLError):
                self.resolve()
        self.assertEqual(list(self.cache.iterdir()), [])

if __name__ == '__main__':
    unittest.main()
