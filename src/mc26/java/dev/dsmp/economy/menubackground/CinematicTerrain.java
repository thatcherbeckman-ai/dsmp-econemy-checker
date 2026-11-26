package dev.dsmp.economy.menubackground;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;
import java.util.Map;

/** A deterministic, textured voxel landscape. Separate from the joined Minecraft world. */
public final class CinematicTerrain {
    private final int w,h; private final double focal,horizon,eye,cx,actorZ;
    private final BufferedImage image; private final double[] depth;
    private final BufferedImage snow,stone,dirt; private boolean shadow;
    private BlockScenery palette=BlockScenery.of(MenuScene.MINECRAFT_SNOW);
    private MenuScene scenery=MenuScene.MINECRAFT_SNOW;
    private BufferedImage trunk,leaves;
    private final boolean foreground;
    private record Vertex(double x,double y,double z,double u,double v){}
    private CinematicTerrain(int w,int h,CharacterComposition options,BufferedImage snow,BufferedImage stone,BufferedImage dirt){
        this(w,h,options,snow,stone,dirt,false);
    }
    private CinematicTerrain(int w,int h,CharacterComposition options,BufferedImage snow,BufferedImage stone,BufferedImage dirt,boolean foreground){
        this.w=w;this.h=h;this.snow=snow;this.stone=stone;this.dirt=dirt;
        this.foreground=foreground;
        var p=CharacterPlacement.of(options,w,h);focal=h*.95;horizon=h*.34;cx=p.centerX();
        actorZ=focal*1.8/p.bodyHeight();eye=(p.feetY()-horizon)*1.8/p.bodyHeight();
        image=new BufferedImage(w,h,foreground?BufferedImage.TYPE_INT_ARGB:BufferedImage.TYPE_INT_RGB);depth=new double[w*h];Arrays.fill(depth,Double.POSITIVE_INFINITY);
    }
    public static BufferedImage create(int w,int h,CharacterComposition options,BufferedImage snow,BufferedImage stone,BufferedImage dirt){
        if(w<1||h<1||w>1024||h>1024)throw new IllegalArgumentException("Overlook image must fit 1024 pixels");
        var scene=new CinematicTerrain(w,h,options,snow,stone,dirt);scene.sky();scene.shadow=options.shadow;scene.terrain();return scene.image;
    }
    public static BufferedImage create(int w,int h,CharacterComposition options,MenuScene scene,Map<String,BufferedImage> textures){
        if(w<1||h<1||w>1024||h>1024)throw new IllegalArgumentException("Overlook image must fit 1024 pixels");
        var p=BlockScenery.of(scene);var top=textures.get(p.top());
        if(p.top().equals("grass_block_top"))top=tint(top,0x8CAD73);
        var result=new CinematicTerrain(w,h,options,top,textures.get(p.rock()),textures.get(p.side()));
        result.palette=p;result.scenery=scene;result.trunk=textures.get(p.trunk());result.leaves=tint(textures.get(p.leaves()),p.foliage());
        result.shadow=options.shadow;result.sky();result.terrain();result.details();result.grade();return result.image;
    }
    public static boolean usesBlocks(MenuBackgroundOptions options){return options.scene==MenuScene.MINECRAFT_SNOW||options.character.sceneryStyle==CharacterComposition.SceneryStyle.BLOCKS&&!options.scene.atlas.isEmpty();}
    public static boolean usesIllustrated(MenuBackgroundOptions options){return options.character.sceneryStyle==CharacterComposition.SceneryStyle.CINEMATIC&&!options.scene.atlas.isEmpty();}
    /** A short, transparent foreground ridge; the detailed illustrated sky and scenery remain visible. */
    public static BufferedImage foreground(int w,int h,CharacterComposition options,MenuScene scene,Map<String,BufferedImage> textures){
        if(w<1||h<1||w>1024||h>1024)throw new IllegalArgumentException("Foreground image must fit 1024 pixels");
        var p=BlockScenery.of(scene);var top=textures.get(p.top());if(p.top().equals("grass_block_top"))top=tint(top,0x8CAD73);
        var result=new CinematicTerrain(w,h,options,top,textures.get(p.rock()),textures.get(p.side()),true);
        result.palette=p;result.scenery=scene;result.shadow=options.shadow;result.terrain();
        int environment=SceneLighting.of(scene).color();
        for(int y=0;y<h;y++)for(int x=0;x<w;x++){
            int index=y*w+x;double z=result.depth[index];if(!Double.isFinite(z)){result.image.setRGB(x,y,0);continue;}
            double t=Math.clamp((z-result.actorZ-1.2)/3.7,0,1),fade=1-t*t*(3-2*t);int alpha=(int)(255*fade),rgb=result.image.getRGB(x,y),value=alpha<<24;
            for(int shift=0;shift<=16;shift+=8){double tint=options.matchLighting?.24:0;int channel=(int)((rgb>>shift&255)*(1-tint)+(environment>>shift&255)*tint);value|=channel<<shift;}
            result.image.setRGB(x,y,value);
        }
        return result.image;
    }
    private static BufferedImage tint(BufferedImage image,int color){var tinted=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++){int rgb=image.getRGB(x,y),value=rgb&0xFF000000;for(int shift=0;shift<=16;shift+=8)value|=((rgb>>shift&255)*(color>>shift&255)/255)<<shift;tinted.setRGB(x,y,value);}return tinted;}
    /** Apply the reference composition explicitly; ordinary scenery selections keep the draft. */
    public static void preset(MenuBackgroundOptions options){
        options.scene=MenuScene.MINECRAFT_SNOW;options.overlay=0;options.blur=0;options.vignette=0;options.brightness=100;
        options.tint=0;options.decorations=1;options.particles=MenuScene.Particles.NONE;
        var c=options.character;c.enabled=true;c.sceneryStyle=CharacterComposition.SceneryStyle.BLOCKS;c.view=CharacterComposition.View.LEFT;c.turn=-15;c.tilt=0;
        c.size=36;c.x=50;c.ground=83;c.platform=CharacterComposition.Platform.SNOW;c.nameplate=true;c.matchLighting=true;
        c.title="";c.subtitle="";
    }
    private void sky(){
        var g=image.createGraphics();try{
            g.setPaint(new GradientPaint(0,0,new Color(palette.sky()),0,(int)horizon,new Color(palette.horizon())));g.fillRect(0,0,w,h);
            var random=new Random(74);if(palette.night())for(int i=0;i<180;i++){int x=random.nextInt(w),y=random.nextInt(Math.max(1,(int)(horizon*.85)));g.setColor(new Color(204,218,255,50+random.nextInt(140)));g.fillRect(x,y,1+(i%11==0?1:0),1);}
            int sunX=(int)(w*.73),sunY=(int)(horizon*.7),radius=Math.max(8,w/32);
            g.setPaint(new RadialGradientPaint(sunX,sunY,radius*5,new float[]{0,.22f,1},new Color[]{new Color(palette.glow()|0xEE000000,true),new Color(palette.glow()|0x36000000,true),new Color(palette.glow()&0xFFFFFF,true)}));g.fillOval(sunX-radius*5,sunY-radius*5,radius*10,radius*10);
            g.setColor(new Color(palette.glow()));g.fillRect(sunX-radius/2,sunY-radius/2,radius,radius);
            if(scenery==MenuScene.AURORA_LAKE||scenery==MenuScene.SPACE_NEBULA){
                for(int i=0;i<9;i++){g.setColor(new Color(palette.glow()|0x0B000000,true));int[] xs=new int[16],ys=new int[16];for(int j=0;j<8;j++){xs[j]=j*w/7;ys[j]=(int)(horizon*(.22+.1*Math.sin(j*.9+i*.16)))+i*3;xs[15-j]=xs[j];ys[15-j]=ys[j]+12;}g.fillPolygon(xs,ys,16);}
            }
            for(int i=0;i<30;i++){int x=random.nextInt(w+100)-50,y=(int)(horizon*(.12+random.nextDouble()*.62));g.setColor(palette.night()?new Color(20,28,46,110):new Color(239,226,221,35));g.fillRect(x,y,22+random.nextInt(95),4+random.nextInt(8));g.fillRect(x+8,y-3,15+random.nextInt(45),4);}
        }finally{g.dispose();}
    }
    private double height(int x,int z){
        // The near ridge remains flat under the character. Beyond it, discrete snow shelves
        // descend into a valley and rise into ridged peaks, rather than a single plinth.
        double edge=actorZ+3.8+1.5*Math.sin(x*.57)+.45*Math.cos(x*1.6);
        if(z<edge){if(Math.abs(x)>1.8&&z<actorZ+1.7)return Math.floor(1.2+.65*Math.sin(x*.7)+.5*Math.cos(z*1.1));return 0;}
        double valley=-7-Math.min(12,(z-edge)*.8);
        double mountains=peak(x,z,-23,56,34,29,28)+peak(x,z,42,87,38,36,33)+peak(x,z,-62,119,44,41,36)+peak(x,z,7,158,30,46,33);
        double value=Math.min(-1,valley)+mountains+1.8*Math.sin(x*.23+z*.16)+1.3*Math.cos(x*.47-z*.28);
        if(palette.kind()==BlockScenery.Kind.DESERT)value=-7+mountains*.25+4*Math.sin(x*.065+z*.09);
        if(palette.kind()==BlockScenery.Kind.OCEAN)value=Math.min(-7,value-8);
        return Math.floor(value);
    }
    private static double peak(double x,double z,double px,double pz,double rx,double rz,double height){return height*Math.exp(-((x-px)*(x-px)/(rx*rx)+(z-pz)*(z-pz)/(rz*rz)));}
    private void terrain(){
        int maxZ=foreground?(int)Math.ceil(actorZ+5):195,minX=foreground?-40:-140,maxX=foreground?40:140;double[][] heights=new double[maxZ+2][maxX-minX+3];
        for(int z=1;z<=maxZ+1;z++)for(int x=minX-1;x<=maxX+1;x++)heights[z][x-minX+1]=height(x,z);
        for(int z=2;z<=maxZ;z++)for(int x=minX;x<=maxX;x++){
            double y=heights[z][x-minX+1];if(palette.kind()==BlockScenery.Kind.SPACE||palette.kind()==BlockScenery.Kind.ISLANDS){if(z>actorZ+7&&Math.sin(x*.12)+Math.cos(z*.1)<.2)continue;}
            if(Math.abs(x)>z*w/focal+4)continue;
            quad(x,y,z,x+1,y,z,x+1,y,z+1,x,y,z+1,snow,.97);
            wall(x,y,z,x+1,y,z,heights[z-1][x-minX+1],.70);
            if(x>=0)wall(x,y,z+1,x,y,z,heights[z][x-minX],.79);
            if(x<0)wall(x+1,y,z,x+1,y,z+1,heights[z][x-minX+2],.86);
        }
    }
    private void wall(double x,double y,double z,double xx,double yy,double zz,double neighbor,double shade){
        if(neighbor>=y)return;
        for(double bottom=y-1;bottom>=neighbor;bottom--){
            BufferedImage texture=bottom==y-1?dirt:bottom>=y-3?dirt:stone;
            quad(x,bottom+1,z,xx,bottom+1,zz,xx,bottom,zz,x,bottom,z,texture,shade);
        }
    }
    private void details(){
        var kind=palette.kind();var random=new Random(2901);
        if(kind==BlockScenery.Kind.OCEAN){
            var water=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);for(int y=0;y<16;y++)for(int x=0;x<16;x++)water.setRGB(x,y,((x*3+y*7)%11==0)?0x639FB2:0x347F9E);
            for(int z=10;z<180;z+=2)for(int x=-120;x<120;x+=2)quad(x,-7,z,x+2,-7,z,x+2,-7,z+2,x,-7,z+2,water,1);
        }
        if(kind==BlockScenery.Kind.CITY){
            var windows=new BufferedImage(16,16,BufferedImage.TYPE_INT_RGB);var painter=windows.createGraphics();try{painter.drawImage(stone,0,0,16,16,null);painter.setColor(new Color(0xD5B2DB));painter.fillRect(3,3,3,5);painter.setColor(new Color(0x9DCEE1));painter.fillRect(10,3,3,5);}finally{painter.dispose();}
            for(int z=20;z<130;z+=9)for(int x=-75;x<75;x+=8){double y=height(x,z),height=8+random.nextInt(19);for(int level=0;level<height;level++)for(int dx=0;dx<3;dx++)for(int dz=0;dz<3;dz++)cube(x+dx,y+level,z+dz,windows,.9);}
        }else if(kind==BlockScenery.Kind.FOREST||kind==BlockScenery.Kind.SNOW||kind==BlockScenery.Kind.BAMBOO||kind==BlockScenery.Kind.ISLANDS){
            for(int z=11;z<91;z+=6)for(int x=-60;x<60;x+=5){if(random.nextDouble()>.33)continue;int xx=x+random.nextInt(3),zz=z+random.nextInt(3);tree(xx,height(xx,zz),zz,4+random.nextInt(4),kind);}
            if(kind==BlockScenery.Kind.FOREST){tree(-4,height(-4,4),4,7,kind);tree(5,height(5,7),7,9,kind);}
        }
        if(scenery==MenuScene.AUTUMN_RIVER||scenery==MenuScene.AURORA_LAKE){
            var water=solid(scenery==MenuScene.AURORA_LAKE?0x91BCCB:0x7398A8);
            for(int z=13;z<145;z++){int center=(int)(7*Math.sin(z*.08)),radius=scenery==MenuScene.AURORA_LAKE?14:3;for(int x=center-radius;x<center+radius;x++)quad(x,-14,z,x+1,-14,z,x+1,-14,z+1,x,-14,z+1,water,1);}
        }
        if(scenery==MenuScene.WINTER_CABIN){
            int x=9,z=19;double y=height(x,z);for(int dy=0;dy<4;dy++)for(int dx=0;dx<5;dx++)for(int dz=0;dz<5;dz++)cube(x+dx,y+dy,z+dz,trunk,.95);
            for(int dx=-1;dx<6;dx++)for(int dz=-1;dz<6;dz++)cube(x+dx,y+4+Math.max(0,2-Math.abs(dx-2)),z+dz,snow,1);
            cube(x+1,y+2,z-1,solid(0xF8D18B),1.1);cube(x+3,y+2,z-1,solid(0xF8D18B),1.1);
        }
        if(scenery==MenuScene.LANTERN_GARDEN){for(int z=10;z<40;z+=7)for(int x:new int[]{-5,5}){double y=height(x,z);for(int dy=0;dy<3;dy++)cube(x,y+dy,z,trunk,.9);cube(x,y+3,z,solid(0xFFD593),1.15);}}
    }
    private static BufferedImage solid(int color){var image=new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);image.setRGB(0,0,color);return image;}
    private void tree(int x,double y,int z,int height,BlockScenery.Kind kind){
        if(kind==BlockScenery.Kind.BAMBOO){for(int i=0;i<height+4;i++)cube(x,y+i,z,trunk,.9);return;}
        for(int i=0;i<height;i++)cube(x,y+i,z,trunk,.87);
        int radius=kind==BlockScenery.Kind.SNOW?1:2;
        for(int dy=-2;dy<=1;dy++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
            if(Math.abs(dx)+Math.abs(dz)>radius+1||dy==1&&(Math.abs(dx)>1||Math.abs(dz)>1))continue;
            cube(x+dx,y+height+dy,z+dz,leaves,.98);
            if(kind==BlockScenery.Kind.SNOW&&dy==1)quad(x+dx,y+height+dy+1.08,z+dz,x+dx+1,y+height+dy+1.08,z+dz,x+dx+1,y+height+dy+1.08,z+dz+1,x+dx,y+height+dy+1.08,z+dz+1,snow,.98);
        }
    }
    private void cube(int x,double y,int z,BufferedImage texture,double light){if(z<2)return;quad(x,y+1,z,x+1,y+1,z,x+1,y+1,z+1,x,y+1,z+1,texture,light);quad(x,y+1,z,x+1,y+1,z,x+1,y,z,x,y,z,texture,light*.75);if(x>=0)quad(x,y+1,z+1,x,y+1,z,x,y,z,x,y,z+1,texture,light*.85);else quad(x+1,y+1,z,x+1,y+1,z+1,x+1,y,z+1,x+1,y,z,texture,light*.85);}
    private void grade(){
        // Distance-only softening keeps nearby block textures sharp and avoids blurring the
        // character's footing. All depth and color work is done once for a cached scene.
        int[] original=image.getRGB(0,0,w,h,null,0,w);
        for(int y=1;y<h-1;y++)for(int x=1;x<w-1;x++){
            int index=y*w+x;double z=depth[index];if(!Double.isFinite(z)||z<17)continue;
            double blend=Math.min(.58,(z-17)/100);int rgb=0;
            for(int shift=0;shift<=16;shift+=8){int sum=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++)sum+=original[index+dy*w+dx]>>shift&255;int value=(int)((original[index]>>shift&255)*(1-blend)+sum/9d*blend);rgb|=value<<shift;}
            image.setRGB(x,y,rgb);
        }
        var g=image.createGraphics();try{
            int x=(int)(w*.73),y=(int)(h*.24),radius=Math.max(w,h);g.setPaint(new RadialGradientPaint(x,y,radius,new float[]{0,.42f,1},new Color[]{new Color(palette.glow()|0x21000000,true),new Color(palette.glow()|0x09000000,true),new Color(0,0,0,25)}));g.fillRect(0,0,w,h);
            if(!palette.night()){
                g.setPaint(new GradientPaint(x,y,new Color(palette.glow()|0x12000000,true),w*.15f,h,new Color(palette.glow()&0xFFFFFF,true)));
                for(int i=0;i<7;i++){int target=(int)(w*(.05+i*.12));g.fillPolygon(new int[]{x-2,x+5,target+w/18,target},new int[]{y,y,h,h},4);}
            }
            g.setPaint(new GradientPaint(0,h*.65f,new Color(palette.fog()&0xFFFFFF,true),0,h,new Color(palette.fog()|0x0D000000,true)));g.fillRect(0,0,w,h);
        }finally{g.dispose();}
    }
    private Vertex project(double x,double y,double z,double u,double v){return new Vertex(cx+focal*x/z,horizon+focal*(eye-y)/z,1/z,u/z,v/z);}
    private void quad(double ax,double ay,double az,double bx,double by,double bz,double cx,double cy,double cz,double dx,double dy,double dz,BufferedImage texture,double shade){
        var a=project(ax,ay,az,0,0);var b=project(bx,by,bz,1,0);var c=project(cx,cy,cz,1,1);var d=project(dx,dy,dz,0,1);
        triangle(a,b,c,texture,shade);triangle(a,c,d,texture,shade);
    }
    private static double edge(Vertex a,Vertex b,double x,double y){return (x-a.x)*(b.y-a.y)-(y-a.y)*(b.x-a.x);}
    private void triangle(Vertex a,Vertex b,Vertex c,BufferedImage texture,double shade){
        double area=edge(a,b,c.x,c.y);if(Math.abs(area)<.00001)return;
        int minX=Math.max(0,(int)Math.floor(Math.min(a.x,Math.min(b.x,c.x)))),maxX=Math.min(w-1,(int)Math.ceil(Math.max(a.x,Math.max(b.x,c.x))));
        int minY=Math.max(0,(int)Math.floor(Math.min(a.y,Math.min(b.y,c.y)))),maxY=Math.min(h-1,(int)Math.ceil(Math.max(a.y,Math.max(b.y,c.y))));
        for(int y=minY;y<=maxY;y++)for(int x=minX;x<=maxX;x++){
            double wa=edge(b,c,x+.5,y+.5)/area,wb=edge(c,a,x+.5,y+.5)/area,wc=1-wa-wb;if(wa<-.00001||wb<-.00001||wc<-.00001)continue;
            double inverse=wa*a.z+wb*b.z+wc*c.z,z=1/inverse;int index=y*w+x;if(z>=depth[index])continue;
            double u=(wa*a.u+wb*b.u+wc*c.u)/inverse,v=(wa*a.v+wb*b.v+wc*c.v)/inverse;
            int rgb=texture.getRGB(Math.clamp((int)(u*texture.getWidth()),0,texture.getWidth()-1),Math.clamp((int)(v*texture.getHeight()),0,texture.getHeight()-1));
            if((rgb>>>24)<96)continue;
            double fog=Math.min(.87,Math.max(0,(z-9)/155));int result=0;double lightScale=shade;
            if(shadow&&shade==.97&&z<actorZ+1.3){double worldX=(x+.5-cx)*z/focal;lightScale-=.29*Math.exp(-worldX*worldX*4.5-(z-actorZ)*(z-actorZ)*16);}
            for(int shift=0;shift<=16;shift+=8){int light=(int)((rgb>>shift&255)*lightScale);int sky=palette.fog()>>shift&255;result|=Math.clamp((int)(light*(1-fog)+sky*fog),0,255)<<shift;}
            image.setRGB(x,y,0xFF000000|result);depth[index]=z;
        }
    }
}
