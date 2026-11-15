#!/usr/bin/env python3
"""Render native-free dashboard draw captures. These are sample-data layout previews."""
import json
from pathlib import Path
import zipfile
from PIL import Image, ImageDraw, ImageFont
ROOT=Path(__file__).resolve().parents[1]
font_dir=ROOT/'build/preview-fonts';font_dir.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(ROOT/'downloads/economy-tracker-0.8.0-mc1.21.11.jar') as jar:
    for filename in ('inter-semibold.ttf','inter-extrabold.ttf'):
        name=next(n for n in jar.namelist() if n.endswith('/'+filename))
        (font_dir/filename).write_bytes(jar.read(name))
fonts={}
for page in ('overview','modules'):
    data=json.loads(Path('/tmp/economy-'+page+'-preview.json').read_text());zoom=2
    image=Image.new('RGB',(data['width']*zoom,data['height']*zoom),(12,14,26));draw=ImageDraw.Draw(image,'RGBA')
    for item in data['draws']:
        color=item['color']&0xffffffff;rgba=(color>>16&255,color>>8&255,color&255,color>>24&255)
        x,y=item['x']*zoom,item['y']*zoom
        if item['kind']=='rect':
            if item['width']<=0 or item['height']<=0:continue
            draw.rectangle((round(x),round(y),round(x+item['width']*zoom)-1,round(y+item['height']*zoom)-1),fill=rgba)
        else:
            size=max(8,round(10*item['scale']*zoom));key=(size,item['bold'])
            if key not in fonts:fonts[key]=ImageFont.truetype(str(font_dir/('inter-extrabold.ttf' if item['bold'] else 'inter-semibold.ttf')),size)
            draw.text((x,y),item['text'],font=fonts[key],fill=rgba,anchor='lt')
    watermark='LAYOUT PREVIEW · SAMPLE DATA';font=ImageFont.truetype(str(font_dir/'inter-semibold.ttf'),12)
    box=draw.textbbox((0,0),watermark,font=font);x=image.width-(box[2]-box[0])-14;y=image.height-19
    draw.rectangle((x-6,y-3,image.width-8,image.height-4),fill=(9,12,22,220));draw.text((x,y),watermark,font=font,fill=(180,190,212,255),anchor='lt')
    destination=ROOT/'build'/f'preview-0.9.0-{page}.png';image.save(destination);print(destination)
