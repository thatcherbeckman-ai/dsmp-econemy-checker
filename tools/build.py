#!/usr/bin/env python3
"""Build the balanced Player Nets display against the verified 0.5.3 release."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'downloads/economy-tracker-0.5.3-mc1.21.11.jar'
BASE_SHA256 = '34cd41dd835b2d9d16442f46db8e7a9d23740a11a245dac75b67714746281cef'
VERSION = '0.5.4'
assert hashlib.sha256(BASE.read_bytes()).hexdigest() == BASE_SHA256, 'Unexpected base JAR'
java_home = Path(os.environ.get('JAVA_HOME', '/workspace/.tools/jdk-21.0.9+10'))
java, javac = str(java_home / 'bin/java'), str(java_home / 'bin/javac')
build = ROOT / 'build'
classes, tests = build / 'classes', build / 'test-classes'
for directory in (classes, tests):
    if directory.exists(): shutil.rmtree(directory)
    directory.mkdir(parents=True)
sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
subprocess.run([javac, '--release', '21', '-cp', str(BASE), '-d', str(classes), *map(str, sources)], check=True)
junit = Path(os.environ.get('JUNIT_JAR', '/workspace/.tools/junit.jar'))
gson_matches = list(Path('/workspace/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson').rglob('gson-*.jar'))
gson = Path(os.environ['GSON_JAR']) if 'GSON_JAR' in os.environ else sorted(gson_matches)[-1]
classpath = os.pathsep.join(map(str, (classes, BASE, gson, junit)))
subprocess.run([javac, '--release', '21', '-cp', classpath, '-d', str(tests), *map(str, sorted((ROOT / 'src/test/java').rglob('*.java')))], check=True)
subprocess.run([java, '-jar', str(junit), 'execute', '--class-path', os.pathsep.join(map(str, (tests, classes, BASE, gson))), '--scan-classpath', '--fail-if-no-tests', '--reports-dir', str(build / 'test-results')], check=True)
asm_matches = list(Path('/workspace/.gradle/wrapper/dists').rglob('asm-9.8.jar'))
asm = Path(os.environ['ASM_JAR']) if 'ASM_JAR' in os.environ else sorted(asm_matches)[-1]
patch_tools = build / 'patch-tools'
patch_tools.mkdir(parents=True, exist_ok=True)
subprocess.run([javac, '--release', '21', '-cp', str(asm), '-d', str(patch_tools), str(ROOT / 'tools/PatchPlayerNets.java')], check=True)
renderer_name = 'dev/dsmp/economy/ui/TrackerRenderer.class'
original_renderer = build / 'original-renderer.class'
with zipfile.ZipFile(BASE) as original:
    original_renderer.write_bytes(original.read(renderer_name))
subprocess.run([java, '-cp', os.pathsep.join(map(str, (patch_tools, asm))), 'PatchPlayerNets', str(original_renderer), str(classes / renderer_name)], check=True)
output = build / f'economy-tracker-{VERSION}-mc1.21.11.jar'
with zipfile.ZipFile(BASE) as original, zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as repaired:
    updated = {str(f.relative_to(classes)): f.read_bytes() for f in classes.rglob('*.class')}
    for name in original.namelist():
        # The replaced implementation has a new nested Confirmation record.
        if name.startswith('dev/dsmp/economy/core/PaymentDetector') and name.endswith('.class'):
            continue
        data = updated.pop(name, original.read(name))
        if name == 'fabric.mod.json':
            metadata = json.loads(data); metadata['version'] = VERSION
            data = (json.dumps(metadata, indent=2) + '\n').encode()
        repaired.writestr(name, data)
    for name, data in sorted(updated.items()): repaired.writestr(name, data)
with zipfile.ZipFile(BASE) as old, zipfile.ZipFile(output) as new:
    assert new.testzip() is None
    for name in new.namelist():
        if name.endswith('.class'):
            assert b'sendCommand' not in new.read(name) and b'sendChat' not in new.read(name), name
        if name != 'fabric.mod.json' and not name.startswith(('dev/dsmp/economy/core/WalletTracker', 'dev/dsmp/economy/core/PaymentDetector', 'dev/dsmp/economy/ui/PlayerNetRows', 'dev/dsmp/economy/ui/TrackerRenderer.class')):
            assert old.read(name) == new.read(name), name
print('Built', output)
print('SHA256', hashlib.sha256(output.read_bytes()).hexdigest())
