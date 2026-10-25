#!/bin/sh
set -eu
TVOS_ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
TVOS_SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
if [ -f "$TVOS_ROOT/local.properties" ]; then
    TVOS_SDK=$(python3 - "$TVOS_ROOT/local.properties" <<'PY'
import sys
from pathlib import Path
for line in Path(sys.argv[1]).read_text().splitlines():
    if line.startswith('sdk.dir='):
        print(line.split('=',1)[1].replace('\\:',':').replace('\\\\','\\'))
        break
PY
)
fi
if [ -z "$TVOS_SDK" ]; then
    echo 'Set ANDROID_HOME or local.properties to your Android SDK.' >&2
    exit 1
fi
"$TVOS_ROOT/gradlew" -p "$TVOS_ROOT" --no-daemon testPreviewUnitTest lintPreview lintRelease assemblePreview assembleRelease
mkdir -p "$TVOS_ROOT/dist"
cp "$TVOS_ROOT/app/build/outputs/apk/preview/app-preview.apk" "$TVOS_ROOT/dist/tvOS-original-preview.apk"
if [ -n "${TVOS_KEYSTORE:-}" ]; then
    rm -f "$TVOS_ROOT/dist/tvOS-original-release-unsigned.apk"
    cp "$TVOS_ROOT/app/build/outputs/apk/release/app-release.apk" "$TVOS_ROOT/dist/tvOS-original-release.apk"
else
    rm -f "$TVOS_ROOT/dist/tvOS-original-release.apk"
    cp "$TVOS_ROOT/app/build/outputs/apk/release/app-release-unsigned.apk" "$TVOS_ROOT/dist/tvOS-original-release-unsigned.apk"
fi
python3 "$TVOS_ROOT/audit.py" "$TVOS_SDK"
