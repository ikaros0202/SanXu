"""Inspect only the explicit Sanxu debug test installation; output contains aggregate evidence."""
from pathlib import Path
import sqlite3,subprocess,json,sys
root=Path(__file__).resolve().parents[1]
serial=sys.argv[1]
if serial not in ['emulator-5582','emulator-5584']:raise SystemExit('Only this task owned test emulators are accepted')
target=root/'artifacts/device.db'
for suffix in ['', '-wal']:
    result=subprocess.run(['adb','-s',serial,'exec-out','run-as','com.sanxu.app','cat','databases/sanxu.db'+suffix],capture_output=True)
    if result.returncode==0:Path(str(target)+suffix).write_bytes(result.stdout)
db=sqlite3.connect(target)
report={'serial':serial,'draws':db.execute('select count(*),max(issue) from draws').fetchone(),'trial':db.execute('select count(*),max(issue) from trial_numbers').fetchone(),'plans':db.execute('select count(*) from plan_entries').fetchone()[0],'caibao':db.execute('select issue,width,height,mimeType from caibao_documents').fetchall(),'liveState':db.execute('select contentType,lastFailureType from live_content_refresh_metadata').fetchall()}
db.close()
(root/'docs/evidence/live-device-data.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,ensure_ascii=False,indent=2))
