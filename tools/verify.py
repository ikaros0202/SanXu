"""Independent, read-only delivery checks. Run from the new project only."""
from pathlib import Path
import hashlib,json,sqlite3,datetime,re,subprocess

root=Path(__file__).resolve().parents[1]
old=Path(r'D:\codexproject\lucky3D')
manifest=json.loads((root/'docs/evidence/original-project-manifest.json').read_text(encoding='utf-8'))
def digest(path): return hashlib.sha256(path.read_bytes()).hexdigest() if path.is_file() else None
changed=[name for name,expected in manifest.items() if digest(old/name)!=expected]
assert not changed, f'Original project changed: {changed}'
current=subprocess.check_output(['git','status','--porcelain'],cwd=old).decode('utf-8').strip()
previous=(root/'docs/evidence/original-git-status.txt').read_text(encoding='utf-8-sig').strip()
assert current.replace('\r\n','\n')==previous.replace('\r\n','\n'), 'Original git status changed'
seed=json.loads((root/'data/fc3d-seed.json').read_text(encoding='utf-8-sig'))
draws=seed['draws']
assert draws[0]['issue']=='2017001'
assert len(draws)==len({d['issue'] for d in draws})==seed['range']['count']
last=None
for draw in draws:
    assert re.fullmatch(r'[0-9]{7}',draw['issue'])
    date=datetime.date.fromisoformat(draw['drawDate'])
    assert str(date.year)==draw['issue'][:4]
    assert len(draw['digits'])==3 and all(type(d) is int and 0<=d<=9 for d in draw['digits'])
    if last:
        assert last['issue']<draw['issue'] and last['drawDate']<draw['drawDate']
        assert int(draw['issue'][4:])==(int(last['issue'][4:])+1 if last['issue'][:4]==draw['issue'][:4] else 1)
    last=draw
dbpath=root/'core/data/src/main/assets/database/sanxu.db'
db=sqlite3.connect(f'file:{dbpath.as_posix()}?mode=ro',uri=True)
assert db.execute('pragma integrity_check').fetchone()[0]=='ok'
assert db.execute('pragma user_version').fetchone()[0]==1
assert db.execute('select count(*) from draws').fetchone()[0]==len(draws)
for table in ['plan_entries','pick_drafts','saved_methods']:
    assert db.execute(f'select count(*) from {table}').fetchone()[0]==0
for file in (root/'core/domain/src/main/kotlin').rglob('*.kt'):
    assert not re.search(r'^import (android\.|androidx\.|com.sanxu.data\.)',file.read_text(encoding='utf-8'),re.M),file
for file in (root/'app/src/main/kotlin').rglob('*.kt'):
    assert not re.search(r'^import com.sanxu.data\.',file.read_text(encoding='utf-8'),re.M),file
report={'originalPathsUnchanged':len(manifest),'originalGitStatusUnchanged':True,'seedRecords':len(draws),'firstIssue':draws[0]['issue'],'lastIssue':draws[-1]['issue'],'seedSha256':digest(root/'data/fc3d-seed.json'),'databaseSha256':digest(dbpath),'databaseIntegrity':'ok','emptyUserTables':True,'dependencyDirection':'passed','checkedAt':datetime.datetime.now().astimezone().isoformat()}
(root/'docs/evidence/data-and-original-verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
