#!/usr/bin/env python3
"""Check distributable APKs without installing or contacting a device."""
from pathlib import Path
import hashlib
import json
import os
import subprocess
import sys
import zipfile

root = Path(__file__).resolve().parent
tools = Path(sys.argv[1]) / 'build-tools' / '34.0.0'
records = []
apks = sorted((root / 'dist').glob('*.apk'))
assert len(apks) == 2, 'Expected exactly one preview and one release APK'
for apk in apks:
    name = apk.name
    preview = 'preview' in name
    badging = subprocess.check_output([str(tools / 'aapt'), 'dump', 'badging', str(apk)], text=True)
    expected = 'com.tvos.launcher.preview' if preview else 'com.tvos.launcher'
    assert "name='" + expected + "'" in badging
    assert "application-label:'tvOS'" in badging
    assert "sdkVersion:'26'" in badging
    assert 'android.permission.INTERNET' not in badging
    assert 'android.permission.WRITE_SECURE_SETTINGS' not in badging
    assert 'com.tvos.launcher.HomeActivity' in badging
    for feature in ('wifi', 'bluetooth', 'touchscreen', 'location'):
        assert "uses-feature-not-required: name='android.hardware." + feature + "'" in badging
    result = subprocess.run([str(tools / 'apksigner'), 'verify', '--print-certs', str(apk)], capture_output=True, text=True)
    signed = preview or bool(os.getenv('TVOS_KEYSTORE'))
    assert (result.returncode == 0) == signed
    with zipfile.ZipFile(apk) as archive:
        assert 'assets/licenses/LICENSE.txt' in archive.namelist()
        assert not any(p.endswith('.so') for p in archive.namelist())
    assert apk.stat().st_size < 250 * 1024, 'Preview exceeded the 250 KiB size budget'
    records.append({'file': name, 'application_id': expected, 'signed': signed,
                    'bytes': apk.stat().st_size, 'sha256': hashlib.sha256(apk.read_bytes()).hexdigest(),
                    'certificate': result.stdout.strip() if signed else 'Unsigned build artifact'})
(root / 'dist' / 'verification.json').write_text(json.dumps(records, indent=2) + '\n')
(root / 'dist' / 'SHA256SUMS').write_text(''.join(r['sha256'] + '  ' + r['file'] + '\n' for r in records))
print(json.dumps(records, indent=2))
