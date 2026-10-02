"""Read and navigate the installed local release on explicitly owned test emulators.

Does not clear app data, install packages, touch Lucky3D, or capture caibao images.
"""
from pathlib import Path
import datetime
import json
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / 'docs/evidence'
OWNED = {'emulator-5582': 37, 'emulator-5584': 26}


def verify(serial):
    assert serial in OWNED, 'Only explicitly owned test emulators are allowed'
    sdk = OWNED[serial]
    output = EVIDENCE / f'release-api{sdk}'
    output.mkdir(exist_ok=True)

    def adb(*args):
        return subprocess.check_output(['adb', '-s', serial, *args], timeout=35).decode('utf-8', errors='replace').strip()

    def tree():
        adb('shell', 'uiautomator', 'dump', '/sdcard/sanxu-release-check.xml')
        return ET.fromstring(adb('shell', 'cat', '/sdcard/sanxu-release-check.xml'))

    def tap(label, attribute='text', bottom=False):
        nodes = [n for n in tree().iter('node') if n.get(attribute) == label]
        assert nodes, f'{serial}: missing control {label}'
        if bottom:
            nodes.sort(key=lambda n: int(re.findall(r'\d+', n.get('bounds'))[1]))
        node = nodes[-1]
        x1, y1, x2, y2 = map(int, re.findall(r'\d+', node.get('bounds')))
        adb('shell', 'input', 'tap', str((x1+x2)//2), str((y1+y2)//2))

    def require(*labels):
        for attempt in range(4):
            texts = [n.get('text', '') for n in tree().iter('node')]
            if all(label in texts for label in labels):
                return texts
            time.sleep(0.4)
        raise AssertionError(f'{serial}: expected {labels}; got {texts}')

    def capture(name):
        # An idle hierarchy round-trip lets Android apply system-bar changes.
        tree()
        data = subprocess.check_output(['adb', '-s', serial, 'exec-out', 'screencap', '-p'], timeout=15)
        assert data.startswith(b'\x89PNG'), 'Invalid native screenshot'
        (output / f'{name}.png').write_bytes(data)

    assert adb('shell', 'getprop', 'ro.build.version.sdk') == str(sdk)
    packages = adb('shell', 'pm', 'list', 'packages').splitlines()
    assert 'package:com.sanxu.app' in packages
    assert 'package:com.lucky3d.app' in packages
    package = adb('shell', 'dumpsys', 'package', 'com.sanxu.app')
    old_package = adb('shell', 'dumpsys', 'package', 'com.lucky3d.app')
    assert 'versionName=1.0.0' in package
    app_user = re.search(r'(?:userId|appId)=(\d+)', package).group(1)
    old_user = re.search(r'(?:userId|appId)=(\d+)', old_package).group(1)
    assert app_user != old_user
    adb('shell', 'am', 'force-stop', 'com.sanxu.app')
    start = adb('shell', 'am', 'start', '-W', '-n', 'com.sanxu.app/com.sanxu.MainActivity')
    assert 'Status: ok' in start
    require('叁序', '首页', '走势', '选号', '方案', '彩报')
    tap('偏好设置', 'content-desc')
    tap('纸白')
    tap('返回', 'content-desc')
    capture('home-light')
    tap('选号', bottom=True)
    require('单选', '组选', '单选单', '位选')
    tap('单选单')
    tap('位选')
    require('百', '十', '个')
    capture('pick-position-light')
    tap('方案', bottom=True)
    require('¥ 26', '10 个号码', '13 注 · 4 条记录')
    capture('plans-light')
    tap('首页', bottom=True)
    tap('偏好设置', 'content-desc')
    tap('墨绿')
    tap('返回', 'content-desc')
    capture('home-dark')
    tap('走势', bottom=True)
    require('走势')
    capture('trend-dark')
    tap('首页', bottom=True)
    tap('偏好设置', 'content-desc')
    tap('纸白')
    tap('返回', 'content-desc')
    assert adb('shell', 'pidof', 'com.sanxu.app')
    crashes = adb('logcat', '-d', '-b', 'crash')
    assert 'com.sanxu.app' not in crashes, 'App crash found in device crash buffer'
    return dict(serial=serial, api=sdk, package='com.sanxu.app', version='1.0.0',
                coinstalled='com.lucky3d.app', newUid=app_user, originalUid=old_user,
                separateAppStorage=True, start=start, fourMethods=True,
                processRestartPreservedPlan=dict(numbers=10, units=13, amountYuan=26),
                themes=['light', 'dark'], crashBufferContainsSanxu=False)


if __name__ == '__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')
    assert len(sys.argv) > 1, 'Pass owned emulator serials explicitly'
    for serial in sys.argv[1:]:
        result = verify(serial)
        result['checkedAt'] = datetime.datetime.now().astimezone().isoformat()
        (EVIDENCE / f'release-smoke-api{OWNED[serial]}.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
        print(json.dumps(result, ensure_ascii=False, indent=2), flush=True)
