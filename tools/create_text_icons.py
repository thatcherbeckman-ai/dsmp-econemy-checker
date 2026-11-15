#!/usr/bin/env python3
"""Original small game icons for the text-row picker. No external image assets."""
from pathlib import Path
from PIL import Image, ImageDraw
import math
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/economy_tracker/textures/text_icons'
ROOT.mkdir(parents=True,exist_ok=True)
S=3
def canvas():
 im=Image.new('RGBA',(64*S,64*S));return im,ImageDraw.Draw(im)
def save(name,im):im.resize((64,64),Image.Resampling.LANCZOS).save(ROOT/(name+'.png'))
def poly(d,points,fill):d.polygon([(round(x*S),round(y*S)) for x,y in points],fill=fill)
def card(d,x,y,color):
 d.rounded_rectangle((x*S,y*S,(x+25)*S,(y+36)*S),radius=4*S,fill='#fff8e9',outline='#30202c',width=2*S)
 poly(d,[(x+12,y+9),(x+18,y+17),(x+12,y+26),(x+6,y+17)],color)
for name in ['cards','joker']:
 im,d=canvas();card(d,10,15,'#e83a62');card(d,29,9,'#263754')
 if name=='joker':poly(d,[(4,26),(13,10),(23,28),(32,10),(37,32)],'#f6c14c');d.ellipse((7*S,37*S,25*S,54*S),fill='#e59a4b',outline='#382632',width=2*S)
 save(name,im)
im,d=canvas();d.ellipse((6*S,6*S,58*S,58*S),fill='#243758',outline='#8aa6ee',width=3*S)
for i in range(12):
 a=i*math.pi/6;b=(i+1)*math.pi/6
 poly(d,[(32,32),(32+23*math.cos(a),32+23*math.sin(a)),(32+23*math.cos(b),32+23*math.sin(b))],['#ce4774','#304d94'][i%2])
d.ellipse((23*S,23*S,41*S,41*S),fill='#d7b568',outline='#fff5d0',width=S);save('roulette',im)
im,d=canvas();d.rounded_rectangle((8*S,11*S,43*S,48*S),radius=7*S,fill='#3277d2',outline='#a0d3ff',width=2*S);d.rounded_rectangle((28*S,22*S,60*S,56*S),radius=7*S,fill='#e84669',outline='#ffb0bc',width=2*S)
for x,y in [(17,21),(32,37),(36,30),(51,48),(44,39)]:d.ellipse(((x-2)*S,(y-2)*S,(x+2)*S,(y+2)*S),fill='#fff9e6')
save('dice',im)
im,d=canvas();poly(d,[(32,4),(54,22),(44,49),(32,61),(20,49),(10,22)],'#28d99e');poly(d,[(32,4),(32,61),(44,49),(54,22)],'#0a967e');poly(d,[(10,22),(54,22),(32,4)],'#84f8db');poly(d,[(32,4),(23,23),(32,61),(42,23)],'#ff4f92');save('gem',im)
im,d=canvas();d.rounded_rectangle((6*S,8*S,31*S,57*S),radius=4*S,fill='#233c3f');d.rounded_rectangle((34*S,8*S,59*S,57*S),radius=4*S,fill='#3d293d');poly(d,[(10,33),(19,17),(28,33),(23,33),(23,49),(15,49),(15,33)],'#71e3af');poly(d,[(37,32),(45,48),(56,32),(50,32),(50,17),(42,17),(42,32)],'#ee8caa');save('arrows',im)
im,d=canvas();d.ellipse((7*S,7*S,57*S,57*S),fill='#c18420',outline='#fff1a5',width=3*S);d.ellipse((13*S,13*S,51*S,51*S),fill='#edbe49',outline='#84501b',width=2*S);d.line((32*S,20*S,32*S,45*S),fill='#fff4b7',width=5*S);d.arc((21*S,18*S,44*S,36*S),70,290,fill='#fff4b7',width=4*S);save('coin',im)
im,d=canvas();poly(d,[(32,57),(5,30),(6,17),(14,9),(24,9),(32,18),(40,9),(51,9),(59,18),(59,30)],'#ed4f82');poly(d,[(13,19),(21,15),(26,20),(19,24),(12,33)],'#ffb1c8');save('heart',im)
print('Created',len(list(ROOT.glob('*.png'))),'original text icons')
