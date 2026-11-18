#!/usr/bin/env python3
"""Prepare/start an isolated offline native 26.3 smoke run using SDL's offscreen driver."""
from pathlib import Path
import json,os,shutil,subprocess,zipfile
from prepare_mc26 import prepare,get
ROOT=Path(__file__).resolve().parents[1];CACHE=prepare();BUILD=ROOT/'build/mc26';game=BUILD/'smoke-game'
# Menus/fonts and built-in pack metadata are enough for this silent offline smoke.
meta=json.loads((CACHE/'version.json').read_text());asset=meta['assetIndex'];index=get(CACHE/'assets/indexes'/f"{asset['id']}.json",asset['url'],sha1=asset['sha1'])
import concurrent.futures
objects=json.loads(index.read_text())['objects'];selected=[o for name,o in objects.items() if not name.startswith(('minecraft/sounds/','minecraft/lang/')) or name=='minecraft/lang/en_us.json']
with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
 futures=[executor.submit(get,CACHE/'assets/objects'/o['hash'][:2]/o['hash'],'https://resources.download.minecraft.net/'+o['hash'][:2]+'/'+o['hash'],o['hash']) for o in selected]
 for future in futures:future.result()
jdk=os.environ.get('ECONOMY_JAVA25') or os.environ.get('JAVA_HOME') or (ROOT.parent/'.tools/economy-jdk25-path.txt').read_text().strip()
mod=ROOT/'build/economy-tracker-0.12.1-mc26.3.jar';classes=BUILD/'probe-classes';classes.mkdir(exist_ok=True)
cp=str(mod)+os.pathsep+(BUILD/'classpath.txt').read_text()
subprocess.run([str(Path(jdk)/'bin/javac'),'--release','25','-cp',cp,'-d',str(classes),str(ROOT/'tools/mc26-smoke/NativeProbe.java')],check=True)
(game/'mods').mkdir(parents=True,exist_ok=True)
for source,name in [(mod,'economy-tracker.jar'),(CACHE/'lib/fabric-api-0.166.2+26.3.jar','fabric-api.jar')]:shutil.copy2(source,game/'mods'/name)
with zipfile.ZipFile(game/'mods/probe.jar','w',zipfile.ZIP_DEFLATED) as z:
 for path in classes.rglob('*.class'):z.write(path,path.relative_to(classes).as_posix())
 z.writestr('fabric.mod.json',json.dumps({'schemaVersion':1,'id':'economy_native_probe','version':'1','environment':'client','entrypoints':{'client':['dev.dsmp.economy.probe.NativeProbe']},'depends':{'economy_tracker':'>=0.12.0','fabric-api':'*'}}))
meta=json.loads((CACHE/'version.json').read_text());libraries=[CACHE/'lib'/Path(x['downloads']['artifact']['path']).name for x in meta['libraries'] if x.get('downloads',{}).get('artifact')]
cp=os.pathsep.join(map(str,[CACHE/'fabric-loader.jar',CACHE/'client.jar',*libraries,*sorted((CACHE/'loader-libs').glob('*.jar'))]))
options=game/'options.txt';text=options.read_text() if options.exists() else '';text='\n'.join(line for line in text.splitlines() if not line.startswith(('guiScale:','soundCategory_master:','renderDistance:','simulationDistance:','maxFps:','enableVsync:','pauseOnLostFocus:')))+'\nguiScale:1\nsoundCategory_master:0.0\nrenderDistance:4\nsimulationDistance:5\nmaxFps:30\nenableVsync:false\npauseOnLostFocus:false\n';options.write_text(text)
args=[str(Path(jdk)/'bin/java'),'-Xmx2G','--enable-native-access=ALL-UNNAMED','-Dfabric.gameJarPath='+str(CACHE/'client.jar'),'-cp',cp,'net.fabricmc.loader.impl.launch.knot.KnotClient','--version','26.3','--gameDir',str(game),'--assetsDir',str(CACHE/'assets'),'--assetIndex',meta['assetIndex']['id'],'--username','EconBuildCheck','--uuid','00000000000000000000000000000001','--accessToken','0','--versionType','release','--width','960','--height','700']
env=os.environ.copy();env['SDL_VIDEODRIVER']='offscreen';env['ALSOFT_DRIVERS']='null';env['XDG_RUNTIME_DIR']=str(game/'runtime');Path(env['XDG_RUNTIME_DIR']).mkdir(exist_ok=True,mode=0o700)
(game/'probe-result.txt').unlink(missing_ok=True)
with (BUILD/'native-runtime.log').open('w') as log:process=subprocess.Popen(args,cwd=game,env=env,stdout=log,stderr=subprocess.STDOUT)
(BUILD/'runtime-pid.txt').write_text(str(process.pid));print('Started isolated offline native smoke:',process.pid)
