package dev.dsmp.economy.menubackground;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/** Perspective-mapped block textures and contact shading, with a ground-connected ledge option. */
public final class PlatformSurface {
    public static final int WIDTH=512,FOOT_Y=128;
    private PlatformSurface(){}
    public static BufferedImage create(BufferedImage top,BufferedImage side,CharacterComposition options,MenuScene scene,int height){
        height=Math.clamp(height,512,2048);var image=new BufferedImage(WIDTH,height,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();
        int bottom=options.base==CharacterComposition.Base.BLOCK?438:height+120,tiles=options.base==CharacterComposition.Base.BLOCK?1:3;
        var topFace=path(256,28,496,128,256,228,16,128);
        Path2D left=path(16,128,256,228,256,bottom,16,bottom-100),right=path(256,228,496,128,496,bottom-100,256,bottom);
        try{
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            int environment=SceneLighting.of(scene).color();BufferedImage topTexture=grade(top,options,environment,1.04),leftTexture=grade(side,options,environment,.72),rightTexture=grade(side,options,environment,.88);
            texture(g,left,leftTexture,new AffineTransform(240d/(side.getWidth()*tiles),100d/(side.getWidth()*tiles),0,210d/side.getHeight(),16,128));
            texture(g,right,rightTexture,new AffineTransform(240d/(side.getWidth()*tiles),-100d/(side.getWidth()*tiles),0,210d/side.getHeight(),256,228));
            texture(g,topFace,topTexture,new AffineTransform(240d/(top.getWidth()*tiles),100d/(top.getWidth()*tiles),-240d/(top.getHeight()*tiles),100d/(top.getHeight()*tiles),256,28));
            g.setColor(new Color(0,0,0,45));g.setStroke(new BasicStroke(2));g.drawLine(16,128,256,228);g.drawLine(256,228,496,128);g.drawLine(256,228,256,bottom);
            if(options.shadow){
                var old=g.getClip();g.clip(topFace);
                g.setColor(new Color(15,19,25,28));double angle=Math.toRadians(options.lightDirection),dx=Math.cos(angle)*120,dy=Math.sin(angle)*54;g.fill(path(232,122,275,126,275+dx,126+dy,232+dx,122+dy));
                for(int i=12;i>=0;i--){double spread=i*(.5+options.shadowSoftness/50d);g.setColor(new Color(8,13,20,3+(12-i)*2));g.fill(new Ellipse2D.Double(221-spread,119-spread*.35,70+spread*2,17+spread*.7));}
                g.setClip(old);
            }
            if(options.base!=CharacterComposition.Base.BLOCK&&(options.platform==CharacterComposition.Platform.GRASS||options.platform==CharacterComposition.Platform.MOSS)){
                for(int i=0;i<48;i++){int x=24+i*10,y=28+(int)(Math.abs(x-256)*.415);int blade=3+i%7;g.setColor(new Color(0x758C53));g.drawLine(x,y+2,x-2,y-blade);g.drawLine(x,y+2,x+2,y-blade/2);}
            }
            if(scene==MenuScene.CHERRY_DAWN||scene==MenuScene.CHERRY_DUSK||scene==MenuScene.LANTERN_GARDEN){var clip=g.getClip();g.clip(topFace);for(int i=0;i<34;i++){int x=37+(i*79)%440,y=50+(i*37)%170;g.setColor(new Color(225,178,196,160));g.fillOval(x,y,3+i%4,2+i%2);}g.setClip(clip);}
        }finally{g.dispose();}return image;
    }
    private static BufferedImage grade(BufferedImage source,CharacterComposition options,int environment,double brightness){
        var result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++){int rgb=source.getRGB(x,y),value=rgb&0xFF000000;double tint=options.matchLighting?.17:0;for(int shift=0;shift<=16;shift+=8){int channel=(int)Math.round((rgb>>shift&255)*brightness*options.lightStrength/100d*(1-tint)+(environment>>shift&255)*tint);value|=Math.clamp(channel,0,255)<<shift;}result.setRGB(x,y,value);}return result;
    }
    private static Path2D path(double... points){var p=new Path2D.Double();p.moveTo(points[0],points[1]);for(int i=2;i<points.length;i+=2)p.lineTo(points[i],points[i+1]);p.closePath();return p;}
    private static void texture(Graphics2D g,Shape face,BufferedImage texture,AffineTransform transform){var saved=g.getTransform();var clip=g.getClip();g.clip(face);g.transform(transform);g.setPaint(new TexturePaint(texture,new Rectangle(0,0,texture.getWidth(),texture.getHeight())));g.fillRect(-4096,-4096,8192,8192);g.setTransform(saved);g.setClip(clip);}
}
