import contextlib
import importlib.util
import io
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('packager', Path(__file__).with_name('package-shared-resources.py'))
packager = importlib.util.module_from_spec(spec)
spec.loader.exec_module(packager)

class ResourcePackagingTest(unittest.TestCase):
    def test_packaging_independent_of_checkout_line_endings(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            source = root / 'source'
            for name in packager.FILES:
                path = source / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_bytes((packager.SOURCE / name).read_bytes().replace(b'\r\n', b'\n'))
            with patch.object(packager, 'SOURCE', source), contextlib.redirect_stdout(io.StringIO()):
                packager.build(root / 'lf')
                for path in source.rglob('*'):
                    if path.is_file():
                        path.write_bytes(path.read_bytes().replace(b'\n', b'\r\n'))
                packager.build(root / 'crlf')
            self.assertEqual(next((root / 'lf').glob('*.zip')).read_bytes(), next((root / 'crlf').glob('*.zip')).read_bytes())
            self.assertEqual((root / 'lf/shared-resources.json').read_bytes(), (root / 'crlf/shared-resources.json').read_bytes())

if __name__ == '__main__':
    unittest.main()
