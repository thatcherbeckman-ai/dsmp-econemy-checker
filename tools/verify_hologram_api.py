#!/usr/bin/env python3
"""Verify hologram API selectors against the official client and Fabric intermediary map.

This checks real class members, visibility, access-widener targets and override legality.
It does not start Minecraft or claim a live rendering test. Inputs are cached downloads.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess

parser=argparse.ArgumentParser()
parser.add_argument('--client',type=Path,required=True)
parser.add_argument('--mappings',type=Path,required=True,help='Extracted official-to-intermediary mappings.tiny')
parser.add_argument('--version-info',type=Path,required=True,help='Official Mojang version metadata JSON')
args=parser.parse_args()
meta=json.loads(args.version_info.read_text())
assert meta['id']=='1.21.11'
assert hashlib.sha1(args.client.read_bytes()).hexdigest()==meta['downloads']['client']['sha1']
classes={};raw=[]
for line in args.mappings.read_text().splitlines():
    parts=line.split('\t')
    if parts[0]=='c':classes[parts[2]]=parts[1]
    elif len(parts)>=5 and parts[0]=='' and parts[1] in ('m','f'):raw.append(parts)
inverse={value:key for key,value in classes.items()}
def mapped(descriptor,mapping):
    return re.sub(r'L([^;]+);',lambda m:'L'+mapping.get(m[1],m[1])+';',descriptor)
aliases={}
for p in raw:aliases.setdefault((p[1],p[4],mapped(p[2],inverse)),set()).add(p[3])
cache={}
javap=Path(os.environ.get('JAVA_HOME','/workspace/.tools/jdk-21.0.9+10'))/'bin/javap'
def members(name):
    if name in cache:return cache[name]
    output=subprocess.run([str(javap),'-p','-s','-classpath',str(args.client),name],text=True,capture_output=True,check=True).stdout
    records={};previous='';parent=None
    for line in output.splitlines():
        text=line.strip()
        if text.endswith('{'):
            match=re.search(r'\bextends\s+([\w.$/]+)',text)
            if match:parent=match[1]
        if text.startswith('descriptor:'):
            descriptor=text.split(':',1)[1].strip()
            member=previous.split('(',1)[0].split()[-1].rstrip(';')
            if member==name or member==name.rsplit('/',1)[-1]:member='<init>'
            records[(member,descriptor)]=previous
        if text:previous=text
    cache[name]=(records,parent)
    return cache[name]
def verify(kind,owner,name,descriptor,access='public',overridable=False):
    original=classes[owner];desc=mapped(descriptor,classes)
    names={'<init>'} if name=='<init>' else aliases.get((kind,name,descriptor),{name})
    while original and original!='java.lang.Object':
        records,parent=members(original)
        for candidate in names:
            header=records.get((candidate,desc))
            if header:
                assert access in header.split(),(owner,name,'expected '+access,header)
                if overridable:assert 'final' not in header.split(),header
                return
        if name=='<init>':break
        original=parent
    raise AssertionError(('Missing native API',kind,owner,name,descriptor,names))
root='net/minecraft/'
def c(name):return root+'class_'+name.replace('$','$class_')
selectors=[
 ('m',c('1542'),'<init>','(L'+c('1937')+';DDDL'+c('1799')+';)V'),
 ('m',c('1542'),'method_6985','()I','public',True),
 ('m',c('1542'),'method_6989','()V'),('m',c('1542'),'method_35190','()V'),
 ('m',c('1297'),'method_5729','(IZ)V','protected'),('f',c('1297'),'field_6012','I'),
 ('m',c('1297'),'method_5838','(I)V'),('m',c('1297'),'method_5628','()I'),
 ('m',c('1297'),'method_5875','(Z)V'),('m',c('1297'),'method_18800','(DDD)V'),('m',c('1297'),'method_5814','(DDD)V'),
 *[('m',c('1297'),name,'()D') for name in ('method_23317','method_23318','method_23321')],
 ('m',c('638'),'method_53875','(L'+c('1297')+';)V'),
 ('m',c('638'),'method_2945','(IL'+c('1297$5529')+';)V'),
 ('m',c('638'),'method_8469','(I)L'+c('1297')+';'),('m',c('638'),'method_27983','()L'+c('5321')+';'),
 ('m',c('5321'),'method_29177','()L'+c('2960')+';'),
 ('m',c('8113$8123'),'<init>','(L'+c('1299')+';L'+c('1937')+';)V'),
 ('f',c('1299'),'field_42457','L'+c('1299')+';'),('f',c('8113$8114'),'field_42409','L'+c('8113$8114')+';'),
 ('f',c('1297$5529'),'field_26999','L'+c('1297$5529')+';'),
 ('m',c('1799'),'method_46651','(I)L'+c('1799')+';'),
 ('f',c('310'),'field_1687','L'+c('638')+';'),('f',c('310'),'field_1724','L'+c('746')+';'),
]
for selector in selectors:verify(*selector)
widener=Path(__file__).resolve().parents[1]/'src/main/resources/economy-tracker.accesswidener'
lines=widener.read_text().splitlines();assert lines[0]=='accessWidener v2 intermediary'
for line in lines[1:]:
    access,kind,owner,name,descriptor=line.split();assert access=='accessible' and kind=='method'
    verify('m',owner,name,descriptor,'private')
print(f'Official 1.21.11 API verified: {len(selectors)} native selectors, {len(lines)-1} private access-widener targets, item-age override and protected client glow flag. Rendering remains untested in a live game.')
