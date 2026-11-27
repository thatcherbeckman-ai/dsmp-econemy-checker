#!/usr/bin/env python3
"""Build and test the real Minecraft 26.3 adapter with Java 25, without API fixtures."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,zipfile
from prepare_mc26 import prepare
ROOT=Path(__file__).resolve().parents[1];CACHE=prepare();BUILD=ROOT/'build/mc26';BUILD.mkdir(parents=True,exist_ok=True)
BASE=ROOT/'downloads/economy-tracker-0.11.0-mc1.21.11.jar'
assert hashlib.sha256(BASE.read_bytes()).hexdigest()=='c97b77a6ef9711caeb5faf9e5fa857f63935cadf249f892ebb9e755dc02a6f6c'
VERSION='0.21.0'
jdk=os.environ.get('ECONOMY_JAVA25') or os.environ.get('JAVA_HOME')
prepared=ROOT.parent/'.tools/economy-jdk25-path.txt'
if not jdk and prepared.exists():jdk=prepared.read_text().strip()
if not jdk:raise RuntimeError('Set ECONOMY_JAVA25 or JAVA_HOME to a full Java 25 JDK')
java=str(Path(jdk)/'bin/java');javac=str(Path(jdk)/'bin/javac')
asm=next((CACHE/'loader-libs').glob('asm-[0-9]*.jar'))
subprocess.run([javac,'-cp',str(asm),'-d',str(BUILD),str(ROOT/'tools/WidenCompileApi.java')],check=True)
game=BUILD/'minecraft-26.3-compile.jar'
subprocess.run([java,'-cp',str(BUILD)+os.pathsep+str(asm),'WidenCompileApi',str(CACHE/'client.jar'),str(game),str(ROOT/'src/mc26/resources/economy-tracker.accesswidener')],check=True)
classpath=os.pathsep.join(map(str,[game,CACHE/'fabric-loader.jar',BASE,*sorted((CACHE/'lib').glob('*.jar')),*sorted((CACHE/'loader-libs').glob('*.jar'))]))
(BUILD/'classpath.txt').write_text(classpath)
classes=BUILD/'classes';shutil.rmtree(classes,ignore_errors=True);classes.mkdir()
native_sources=list((ROOT/'src/mc26/java').rglob('*.java'));overrides={p.relative_to(ROOT/'src/mc26/java') for p in native_sources}
sources=[p for p in (ROOT/'src/main/java').rglob('*.java') if 'net.minecraft' not in p.read_text() and p.relative_to(ROOT/'src/main/java') not in overrides]+native_sources
subprocess.run([javac,'-proc:none','--release','25','-cp',classpath,'-d',str(classes),*map(str,sources)],check=True)
junit=Path(os.environ.get('JUNIT_JAR',str(ROOT.parent/'.tools/junit.jar')))
tests=BUILD/'test-classes';shutil.rmtree(tests,ignore_errors=True);tests.mkdir()
cp=os.pathsep.join([str(classes),classpath,str(junit)])
subprocess.run([javac,'--release','25','-cp',cp,'-d',str(tests),*map(str,[*(ROOT/'src/test/java').rglob('*.java'),*(ROOT/'src/mc26/test/java').rglob('*.java')])],check=True)
subprocess.run([java,'-jar',str(junit),'execute','--class-path',os.pathsep.join([str(tests),str(classes),classpath]),'--scan-classpath',str(tests),'--fail-if-no-tests','--reports-dir',str(BUILD/'test-results')],check=True)
output=ROOT/'build'/f'economy-tracker-{VERSION}-mc26.3.jar'
native={p.relative_to(ROOT/'src/mc26/java').with_suffix('').as_posix() for p in (ROOT/'src/mc26/java').rglob('*.java')}
updated={f.relative_to(classes).as_posix():f.read_bytes() for f in classes.rglob('*.class') if f.relative_to(classes).as_posix().split('$')[0].removesuffix('.class') in native}
for resource in (ROOT/'src/mc26/resources').rglob('*'):
    if resource.is_file():updated[resource.relative_to(ROOT/'src/mc26/resources').as_posix()]=resource.read_bytes()
with zipfile.ZipFile(BASE) as original,zipfile.ZipFile(output,'w',zipfile.ZIP_DEFLATED) as target:
    for name in original.namelist():
        raw=original.read(name)
        if name.endswith('.class') and name not in updated and b'net/minecraft/' in raw:raise RuntimeError('Unported Minecraft adapter: '+name)
        data=updated.pop(name,raw)
        if name=='fabric.mod.json':
            info=json.loads(data);info['version']=VERSION;info['name']='thatchacats econemy checker';info['description']='Client-side economy tracking, editable HUD, styled text and auction holograms for Minecraft 26.3.'
            info['mixins']=['economy-tracker-inventory.mixins.json']
            info['depends'].update({'fabricloader':'>=0.20.1','minecraft':'~26.3','java':'>=25','fabric-api':'>=0.166.2'})
            data=(json.dumps(info,indent=2)+'\n').encode()
        target.writestr(name,data)
    for name,data in sorted(updated.items()):target.writestr(name,data)
with zipfile.ZipFile(output) as archive:
    assert archive.testzip() is None and len(archive.namelist())==len(set(archive.namelist()))
    assert archive.read('economy-tracker.accesswidener').startswith(b'accessWidener v2 official\n')
    for name in archive.namelist():
        assert not name.startswith(('net/','org/','com/','tools/','dev/dsmp/economy/probe/')),name
        if name.endswith('.class'):
            data=archive.read(name);assert b'net/minecraft/class_' not in data,name
            assert b'sendCommand' not in data and b'sendChat' not in data,name
# Exercise the packaged deliverable; shared business/data classes remain byte-for-byte identical.
actual_cp=os.pathsep.join([str(tests),str(output),classpath.replace(str(BASE)+os.pathsep,'')])
subprocess.run([java,'-jar',str(junit),'execute','--class-path',actual_cp,'--scan-classpath',str(tests),'--fail-if-no-tests','--details','summary','--reports-dir',str(BUILD/'packaged-test-results')],check=True)
print('Built',output);print('SHA256',hashlib.sha256(output.read_bytes()).hexdigest())
