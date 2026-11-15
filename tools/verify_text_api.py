#!/usr/bin/env python3
"""Verify text/image/font API selectors against the official client and Fabric intermediary map.

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
 ('m',c('332'),'method_25293','(Lcom/mojang/blaze3d/pipeline/RenderPipeline;L'+c('2960')+';IIFFIIIIIII)V'),
 ('f',c('10799'),'field_56883','Lcom/mojang/blaze3d/pipeline/RenderPipeline;'),
 ('m',c('1011'),'method_4309','(Ljava/io/InputStream;)L'+c('1011')+';'),
 ('m',c('1043'),'<init>','(Ljava/util/function/Supplier;L'+c('1011')+';)V'),
 ('m',c('1043'),'close','()V'),('m',c('1011'),'close','()V'),
 ('m',c('1060'),'method_4616','(L'+c('2960')+';L'+c('1044')+';)V'),
 ('m',c('1060'),'method_4615','(L'+c('2960')+';)V'),
 ('m',c('310'),'method_1531','()L'+c('1060')+';'),
 ('m',c('11719$11721'),'<init>','(L'+c('2960')+';)V'),
 ('m',c('2583'),'method_27704','(L'+c('11719')+';)L'+c('2583')+';'),
 ('m',c('5250'),'method_27694','(Ljava/util/function/UnaryOperator;)L'+c('5250')+';'),
 ('m',c('327'),'method_27525','(L'+c('5348')+';)I'),
 ('m',c('156'),'method_668','()L'+c('156$158')+';'),
 ('m',c('156$158'),'method_60932','(Ljava/nio/file/Path;)V'),
]
for selector in selectors:verify(*selector)
widener=Path(__file__).resolve().parents[1]/'src/main/resources/economy-tracker.accesswidener'
lines=widener.read_text().splitlines();assert lines[0]=='accessWidener v2 intermediary'
for line in lines[1:]:
    access,kind,owner,name,descriptor=line.split();assert access=='accessible' and kind=='method'
    verify('m',owner,name,descriptor,'private')
print(f'Official 1.21.11 text UI API verified: {len(selectors)} texture/font/dialog selectors and {len(lines)-1} retained private access-widener targets. Rendering remains untested in a live game.')
