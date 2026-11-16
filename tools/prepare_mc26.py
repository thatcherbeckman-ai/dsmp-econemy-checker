#!/usr/bin/env python3
"""Cache the official 26.3 SDK and pinned Fabric dependencies. Nothing is bundled into the mod."""
from pathlib import Path
import concurrent.futures,hashlib,json,os,urllib.request,zipfile
ROOT=Path(__file__).resolve().parents[1]
CACHE=Path(os.environ.get('ECONOMY_MC26_CACHE',str(ROOT.parent/'.tools/economy26')))
GAME='26.3';LOADER='0.20.1';API='0.166.2+26.3'
CLIENT_SHA1='e877b6a07acd633fb3bb475002175cec036e7b87'
LOADER_SHA256='d0c5a082e07410aaff9de7ab196aa6b4236002870f9ba3ad5c1260fcce5110ad'
API_SHA256='e8acacc7836f39237c83c2a664b039647ee36f93822797acc40cda44666002ea'
def get(path,url,sha1=None,sha256=None):
    path=Path(path);path.parent.mkdir(parents=True,exist_ok=True)
    if not path.exists():
        with urllib.request.urlopen(url,timeout=120) as response:
            temporary=path.with_suffix(path.suffix+'.part')
            try:
                with temporary.open('wb') as output:
                    while data:=response.read(1024*1024):output.write(data)
                temporary.replace(path)
            finally:temporary.unlink(missing_ok=True)
    data=path.read_bytes()
    if sha1 and hashlib.sha1(data).hexdigest()!=sha1:raise RuntimeError('Unexpected dependency hash: '+str(path))
    if sha256 and hashlib.sha256(data).hexdigest()!=sha256:raise RuntimeError('Unexpected dependency hash: '+str(path))
    return path

def prepare():
    CACHE.mkdir(parents=True,exist_ok=True);version=CACHE/'version.json'
    if not version.exists():
        manifest=json.load(urllib.request.urlopen('https://piston-meta.mojang.com/mc/game/version_manifest_v2.json',timeout=30))
        url=next(v['url'] for v in manifest['versions'] if v['id']==GAME);get(version,url)
    meta=json.loads(version.read_text());assert meta['id']==GAME and meta['javaVersion']['majorVersion']==25
    assert meta['downloads']['client']['sha1']==CLIENT_SHA1
    get(CACHE/'client.jar',meta['downloads']['client']['url'],sha1=CLIENT_SHA1)
    loader=get(CACHE/'fabric-loader.jar',f'https://maven.fabricmc.net/net/fabricmc/fabric-loader/{LOADER}/fabric-loader-{LOADER}.jar',sha256=LOADER_SHA256)
    api=get(CACHE/'lib'/f'fabric-api-{API}.jar',f'https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/{API}/fabric-api-{API}.jar',sha256=API_SHA256)
    jobs=[]
    for lib in meta['libraries']:
        artifact=lib.get('downloads',{}).get('artifact')
        if artifact:jobs.append((CACHE/'lib'/Path(artifact['path']).name,artifact['url'],artifact.get('sha1'),None))
    launcher=CACHE/'loader.json'
    if not launcher.exists():get(launcher,f'https://meta.fabricmc.net/v2/versions/loader/{GAME}/{LOADER}')
    loader_meta=json.loads(launcher.read_text())['launcherMeta']
    for lib in loader_meta['libraries']['common']+loader_meta['libraries'].get('client',[]):
        group,name,ver=lib['name'].split(':');url=lib['url']+group.replace('.','/')+'/'+name+'/'+ver+'/'+name+'-'+ver+'.jar'
        jobs.append((CACHE/'loader-libs'/f'{name}-{ver}.jar',url,lib.get('sha1'),lib.get('sha256')))
    with concurrent.futures.ThreadPoolExecutor(max_workers=8) as executor:
        futures=[executor.submit(get,*job) for job in jobs]
        for future in futures:future.result()
    with zipfile.ZipFile(api) as archive:
        for name in archive.namelist():
            if name.startswith('META-INF/jars/') and name.endswith('.jar'):
                (CACHE/'lib'/Path(name).name).write_bytes(archive.read(name))
    return CACHE

if __name__=='__main__':print('Prepared official SDK and Fabric dependencies:',prepare())
