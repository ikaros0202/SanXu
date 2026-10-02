from pathlib import Path
import json,xml.etree.ElementTree as ET,hashlib
root=Path(__file__).resolve().parents[1]
modules={}
for path in ['core/domain/build/test-results/test','core/data/build/test-results/testDebugUnitTest','app/build/test-results/testDebugUnitTest']:
    suites=[ET.parse(p).getroot() for p in (root/path).glob('TEST-*.xml')]
    modules[path]={k:sum(int(s.attrib.get(k,0)) for s in suites) for k in ['tests','failures','errors','skipped']}
apk={p.name:dict(bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest()) for p in (root/'artifacts').glob('*.apk')}
report=dict(jvm=modules,apks=apk)
(root/'docs/evidence/test-and-apk-summary.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
print(json.dumps(report,indent=2))
