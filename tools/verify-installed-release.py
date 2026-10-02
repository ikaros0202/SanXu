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
VERSION = '1.0.2'


def verify(serial):
    assert serial in OWNED, 'Only explicitly owned test emulators are allowed'
    sdk = OWNED[serial]
    output = EVIDENCE / f'release-{VERSION}-api{sdk}'
    output.mkdir(exist_ok=True)

    def adb(*args):
        return subprocess.check_output(['adb', '-s', serial, *args], timeout=35).decode('utf-8', errors='replace').strip()

    def tree():
        # Window transitions can briefly have no root; never read a stale prior dump.
        for attempt in range(4):
            adb('shell', 'rm', '-f', '/sdcard/sanxu-release-check.xml')
            result = adb('shell', 'uiautomator', 'dump', '/sdcard/sanxu-release-check.xml')
            if 'dumped to:' in result:
                return ET.fromstring(adb('shell', 'cat', '/sdcard/sanxu-release-check.xml'))
            time.sleep(0.3)
        raise AssertionError(f'{serial}: no current UI hierarchy after four attempts')

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
    assert f'SanXu_Design_API{sdk}' in adb('emu', 'avd', 'name'), 'Check dedicated AVD ownership'
    packages = adb('shell', 'pm', 'list', 'packages').splitlines()
    assert 'package:com.sanxu.app' in packages
    assert 'package:com.lucky3d.app' in packages
    package = adb('shell', 'dumpsys', 'package', 'com.sanxu.app')
    old_package = adb('shell', 'dumpsys', 'package', 'com.lucky3d.app')
    assert f'versionName={VERSION}' in package
    app_user = re.search(r'(?:userId|appId)=(\d+)', package).group(1)
    old_user = re.search(r'(?:userId|appId)=(\d+)', old_package).group(1)
    assert app_user != old_user
    adb('shell', 'am', 'force-stop', 'com.sanxu.app')
    start = adb('shell', 'am', 'start', '-W', '-n', 'com.sanxu.app/com.sanxu.MainActivity')
    assert 'Status: ok' in start
    require('叁序', '首页', '走势', '选号', '方案', '彩报')
    tap('偏好设置', 'content-desc')
    tap('浅色')
    tap('返回', 'content-desc')
    capture('home-light')
    tap('选号', bottom=True)
    require('单选', '组选', '单选单', '位选')
    tap('单选单')
    tap('位选')
    require('百位', '十位', '个位')
    tap('清空')
    tap('0', 'content-desc'); tap('1', 'content-desc')
    tap('下一位'); tap('0', 'content-desc')
    tap('下一位'); tap('2', 'content-desc'); tap('7', 'content-desc')
    require('4 个号码 · 4 注', '¥ 8')
    tap('十位')
    capture('pick-position-light')
    # Verify the shared sheet still closes by Back and a real downward drag.
    tap('玩法说明', 'content-desc')
    require('四种选号方式')
    adb('shell', 'input', 'keyevent', '4')
    require('4 个号码 · 4 注')
    assert not any(n.get('text') == '四种选号方式' for n in tree().iter('node'))
    tap('玩法说明', 'content-desc')
    require('四种选号方式')
    close = next(n for n in tree().iter('node') if n.get('content-desc') == '关闭弹层')
    _, y1, _, y2 = map(int, re.findall(r'\d+', close.get('bounds')))
    screen = adb('shell', 'wm', 'size')
    width, height = map(int, re.findall(r'(\d+)x(\d+)', screen)[-1])
    adb('shell', 'input', 'swipe', str(width//2), str((y1+y2)//2), str(width//2), str(height-30), '320')
    require('4 个号码 · 4 注')
    assert not any(n.get('text') == '四种选号方式' for n in tree().iter('node'))
    tap('方案', bottom=True)
    require('¥ 26', '10 个号码', '13 注 · 4 条记录')
    capture('plans-light')
    tap('首页', bottom=True)
    tap('偏好设置', 'content-desc')
    tap('深色')
    tap('返回', 'content-desc')
    capture('home-dark')
    tap('选号', bottom=True)
    tap('十位')
    require('4 个号码 · 4 注', '¥ 8')
    capture('pick-position-dark')
    tap('走势', bottom=True)
    require('走势')
    capture('trend-dark')
    tap('首页', bottom=True)
    tap('偏好设置', 'content-desc')
    tap('浅色')
    tap('返回', 'content-desc')
    assert adb('shell', 'pidof', 'com.sanxu.app')
    crashes = adb('logcat', '-d', '-b', 'crash')
    assert 'com.sanxu.app' not in crashes, 'App crash found in device crash buffer'
    return dict(serial=serial, api=sdk, package='com.sanxu.app', version=VERSION,
                coinstalled='com.lucky3d.app', newUid=app_user, originalUid=old_user,
                separateAppStorage=True, start=start, fourMethods=True,
                processRestartPreservedPlan=dict(numbers=10, units=13, amountYuan=26),
                guidedPick=dict(numbers=['002','007','102','107'],amountYuan=8),
                sheetBackAndDragDismiss=True,
                themes=['light', 'dark'], crashBufferContainsSanxu=False)


if __name__ == '__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')
    assert len(sys.argv) > 1, 'Pass owned emulator serials explicitly'
    for serial in sys.argv[1:]:
        result = verify(serial)
        result['checkedAt'] = datetime.datetime.now().astimezone().isoformat()
        (EVIDENCE / f'release-{VERSION}-smoke-api{OWNED[serial]}.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
        print(json.dumps(result, ensure_ascii=False, indent=2), flush=True)
