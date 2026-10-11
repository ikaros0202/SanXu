"""Build a deterministic tools ZIP and its consumer lock, without publishing."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'shared/fc3d-tools'
FILES = ['manifest.json', 'testdata/fc3d-golden-cases.json', 'tools/fetch-caiba-trial-seed.py', 'tools/fetch-fc3d-seed.ps1', 'tools/validate-predevelopment-data.ps1']

def build(output):
    version = json.loads((SOURCE / 'manifest.json').read_text(encoding='utf-8'))['version']
    output.mkdir(parents=True, exist_ok=True)
    target = output / f'fc3d-tools-{version}.zip'
    content = {name: (SOURCE / name).read_bytes() for name in FILES}
    with zipfile.ZipFile(target, 'w', compression=zipfile.ZIP_STORED) as archive:
        for name, data in content.items():
            entry = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            entry.create_system = 3
            entry.external_attr = 0o100644 << 16
            archive.writestr(entry, data)
    lock = {'version': version, 'url': f'https://github.com/ikaros0202/SanXu/releases/download/tools-v{version}/{target.name}', 'sha256': hashlib.sha256(target.read_bytes()).hexdigest(), 'files': {name: hashlib.sha256(data).hexdigest() for name, data in content.items()}}
    (output / 'shared-resources.json').write_text(json.dumps(lock, indent=2) + '\n', encoding='utf-8')
    (output / (target.name + '.sha256')).write_text(lock['sha256'] + '  ' + target.name + '\n', encoding='utf-8')
    print(json.dumps({'archive': str(target), 'sha256': lock['sha256']}, indent=2))

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--output', type=Path, default=ROOT / 'artifacts/resource-packages')
    build(parser.parse_args().output)
