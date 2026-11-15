#!/usr/bin/env python3
"""Build movable gradient text boxes and Minecraft glass against the verified 0.7.0 release."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'downloads/economy-tracker-0.7.0-mc1.21.11.jar'
BASE_SHA256 = 'a2ca79ff5124bc1bfe08dafbb8ba6afd11bb5dc9e6269e7089b8be6347083de7'
VERSION = '0.8.0'
assert hashlib.sha256(BASE.read_bytes()).hexdigest() == BASE_SHA256, 'Unexpected base JAR'
java_home = Path(os.environ.get('JAVA_HOME', '/workspace/.tools/jdk-21.0.9+10'))
java, javac = str(java_home / 'bin/java'), str(java_home / 'bin/javac')
build = ROOT / 'build'
classes, tests = build / 'classes', build / 'test-classes'
for directory in (classes, tests):
    if directory.exists(): shutil.rmtree(directory)
    directory.mkdir(parents=True)
joml = Path(os.environ.get('JOML_JAR', '/workspace/.tools/joml-1.10.8.jar'))
api_stubs = build / 'minecraft-api-fixture'
if api_stubs.exists(): shutil.rmtree(api_stubs)
api_stubs.mkdir(parents=True)
subprocess.run([javac, '--release', '21', '-cp', str(joml), '-d', str(api_stubs), *map(str, sorted((ROOT / 'tools/minecraft-api-fixture').rglob('*.java')))], check=True)
brigadier = Path(os.environ.get('BRIGADIER_JAR', '/workspace/.tools/brigadier-1.3.10.jar'))
gson_matches = list(Path('/workspace/.gradle/caches/modules-2/files-2.1/com.google.code.gson/gson').rglob('gson-*.jar'))
gson = Path(os.environ['GSON_JAR']) if 'GSON_JAR' in os.environ else sorted(gson_matches)[-1]
base_classpath = os.pathsep.join(map(str, (BASE, brigadier, gson, api_stubs, joml)))
sources = sorted((ROOT / 'src/main/java').rglob('*.java'))
subprocess.run([javac, '--release', '21', '-cp', base_classpath, '-d', str(classes), *map(str, sources)], check=True)
junit = Path(os.environ.get('JUNIT_JAR', '/workspace/.tools/junit.jar'))
classpath = os.pathsep.join(map(str, (classes, BASE, gson, junit, brigadier, api_stubs, joml)))
subprocess.run([javac, '--release', '21', '-cp', classpath, '-d', str(tests), *map(str, sorted((ROOT / 'src/test/java').rglob('*.java')))], check=True)
subprocess.run([java, '-jar', str(junit), 'execute', '--class-path', os.pathsep.join(map(str, (tests, classes, BASE, gson, brigadier, api_stubs, joml))), '--scan-classpath', '--fail-if-no-tests', '--reports-dir', str(build / 'test-results')], check=True)
asm_matches = list(Path('/workspace/.gradle/wrapper/dists').rglob('asm-9.8.jar'))
asm = Path(os.environ['ASM_JAR']) if 'ASM_JAR' in os.environ else sorted(asm_matches)[-1]
patch_tools = build / 'patch-tools'
patch_tools.mkdir(parents=True, exist_ok=True)
asm_tree_matches = list(Path('/workspace/.gradle/wrapper/dists').rglob('asm-tree-9.8.jar'))
asm_tree = Path(os.environ['ASM_TREE_JAR']) if 'ASM_TREE_JAR' in os.environ else sorted(asm_tree_matches)[-1]
asm_cp = os.pathsep.join(map(str, (asm, asm_tree)))
subprocess.run([javac, '--release', '21', '-cp', asm_cp, '-d', str(patch_tools), str(ROOT / 'tools/PatchLoans.java'), str(ROOT / 'tools/PatchRakebackCommand.java'), str(ROOT / 'tools/PatchNoLoanLabels.java'), str(ROOT / 'tools/PatchAppearance.java'), str(ROOT / 'tools/PatchTextBoxes.java')], check=True)
patch_classpath = os.pathsep.join(map(str, (patch_tools, asm, asm_tree)))
original_classes = build / 'original-classes'
original_classes.mkdir(parents=True, exist_ok=True)
patch_names = ['dev/dsmp/economy/EconomyTrackerClient.class', 'dev/dsmp/economy/LocalStore.class', 'dev/dsmp/economy/ui/TrackerRenderer.class', 'dev/dsmp/economy/ui/TrackerScreen.class', 'dev/dsmp/economy/ui/TrackerRenderer$Ink.class']
with zipfile.ZipFile(BASE) as original:
    for name in patch_names:
        destination = original_classes / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(original.read(name))
subprocess.run([java, '-cp', patch_classpath, 'PatchTextBoxes', str(original_classes), str(classes)], check=True)
# Execute the generated Minecraft adapters in native-free fixtures with JVM verification.
fixture = build / 'command-fixture'
if fixture.exists(): shutil.rmtree(fixture)
fixture.mkdir(parents=True)
fixture_cp = os.pathsep.join(map(str, (classes, BASE, brigadier, gson, api_stubs, joml)))
subprocess.run([javac, '--release', '21', '-cp', fixture_cp, '-d', str(fixture), *map(str, sorted((ROOT / 'tools/fixtures').rglob('*.java')))], check=True)
fixture_client = fixture / patch_names[0]
subprocess.run([java, '-cp', patch_classpath, 'PatchRakebackCommand', str(fixture_client), str(fixture_client)], check=True)
fixture_store = fixture / patch_names[1]
fixture_store.parent.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(ROOT / 'downloads/economy-tracker-0.5.5-mc1.21.11.jar') as legacy:
    fixture_store.write_bytes(legacy.read(patch_names[1]))
subprocess.run([java, '-cp', patch_classpath, 'PatchLoans', str(fixture), str(fixture)], check=True)
subprocess.run([java, '-cp', patch_classpath, 'PatchNoLoanLabels', str(fixture / patch_names[2]), str(fixture / patch_names[2])], check=True)
subprocess.run([java, '-cp', patch_classpath, 'PatchAppearance', str(fixture), str(fixture)], check=True)
subprocess.run([java, '-cp', patch_classpath, 'PatchTextBoxes', str(fixture), str(fixture)], check=True)
for smoke in ('RakebackPatchSmoke', 'LoanPatchSmoke', 'AppearancePatchSmoke', 'TextBoxPatchSmoke'):
    subprocess.run([java, '-Xverify:all', '-cp', os.pathsep.join(map(str, (fixture, classes, BASE, brigadier, gson, api_stubs, joml))), smoke], check=True)
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
    assert not any(n.startswith(('net/', 'tools/')) or n.endswith('Smoke.class') for n in new.namelist()), 'Fixture leaked into mod'
    for name in new.namelist():
        if name.endswith('.class'):
            assert b'sendCommand' not in new.read(name) and b'sendChat' not in new.read(name), name
        if name != 'fabric.mod.json' and not name.startswith(('dev/dsmp/economy/core/WalletTracker', 'dev/dsmp/economy/core/PaymentDetector', 'dev/dsmp/economy/ui/PlayerNetRows', 'dev/dsmp/economy/ui/TrackerRenderer', 'dev/dsmp/economy/EconomyTrackerClient.class', 'dev/dsmp/economy/commands/', 'dev/dsmp/economy/core/RakebackReset', 'dev/dsmp/economy/loans/', 'dev/dsmp/economy/TrackerConfig', 'dev/dsmp/economy/core/ViewCycle', 'dev/dsmp/economy/LocalStore.class', 'dev/dsmp/economy/ui/TrackerScreen.class', 'dev/dsmp/economy/ui/AppearanceScreen', 'dev/dsmp/economy/appearance/', 'dev/dsmp/economy/text/', 'dev/dsmp/economy/ui/TextBoxesScreen', 'dev/dsmp/economy/ui/TextHud', 'dev/dsmp/economy/ui/GlassPane')):
            assert old.read(name) == new.read(name), name
print('Built', output)
print('SHA256', hashlib.sha256(output.read_bytes()).hexdigest())
