from pathlib import Path
import json
root=Path(__file__).resolve().parents[1]
draws=json.loads((root/'data/fc3d-seed.json').read_text(encoding='utf-8-sig'))['draws']
(root/'docs/prototype/history.js').write_text('const historyData='+json.dumps(draws[-31:],ensure_ascii=False)+';',encoding='utf-8')
print(f'Prototype history refreshed through {draws[-1]["issue"]}')
