#!/usr/bin/env python3
"""Native-free style preview with the shipped bubble font and original game icons."""
import json,zipfile
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont
ROOT=Path(__file__).resolve().parents[1]
resource=ROOT/'src/main/resources/assets/economy_tracker'
fonts_dir=ROOT/'build/preview-fonts';fonts_dir.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(ROOT/'downloads/economy-tracker-0.10.1-mc1.21.11.jar') as z:
 for name in ('inter-semibold.ttf','inter-extrabold.ttf'):
  (fonts_dir/name).write_bytes(z.read('assets/economy_tracker/font/'+name))
data=json.loads(Path('/tmp/styled-text-preview.json').read_text());zoom=2
image=Image.new('RGB',(data['width']*zoom,data['height']*zoom),(45,49,56));draw=ImageDraw.Draw(image,'RGBA')
# Neutral backdrop to expose alpha, without suggesting this is a live-game capture.
for y in range(image.height):
 v=int(42+17*y/image.height);draw.line((0,y,image.width,y),fill=(v,v+4,v+8,255))
fonts={}
for item in data['draws']:
 c=item['color']&0xffffffff;color=(c>>16&255,c>>8&255,c&255,c>>24&255);x,y=item['x']*zoom,item['y']*zoom
 if item['kind']=='rect':
  if item['width']>0 and item['height']>0:draw.rectangle((round(x),round(y),round(x+item['width']*zoom)-1,round(y+item['height']*zoom)-1),fill=color)
 elif item['kind']=='image':
  name=item['text'].split('/')[-1];path=resource/'textures/text_icons'/name
  if path.is_file():
   icon=Image.open(path).convert('RGBA').resize((max(1,round(item['width']*zoom)),max(1,round(item['height']*zoom))),Image.Resampling.LANCZOS);image.paste(icon,(round(x),round(y)),icon)
 else:
  size=max(6,round(10*item['scale']*zoom));bubble=item.get('font')=='economy_tracker:bubble';key=(size,item['bold'],bubble)
  if key not in fonts:fonts[key]=ImageFont.truetype(str(resource/'font/bubble-lilita-one.ttf' if bubble else fonts_dir/('inter-extrabold.ttf' if item['bold'] else 'inter-semibold.ttf')),size)
  draw.text((x,y),item['text'],font=fonts[key],fill=color,anchor='lt')
font=ImageFont.truetype(str(fonts_dir/'inter-semibold.ttf'),16);draw.text((16,image.height-28),'LAYOUT PREVIEW · SAMPLE TEXT · NOT IN-GAME',font=font,fill=(216,221,234,255))
destination=ROOT/'build/preview-0.11.0-text-lines.png';image.convert('RGB').save(destination);print(destination)
