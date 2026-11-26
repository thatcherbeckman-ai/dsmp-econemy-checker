package dev.dsmp.economy.ui;

import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.resources.DefaultPlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
import java.util.function.Supplier;

/** A new render-state snapshot, never a modified player/entity or third-person camera switch. */
public final class CharacterSceneRenderer {
    private static UUID profile;private static Supplier<PlayerSkin> cachedSkin;
    private CharacterSceneRenderer(){}
    public static void draw(GuiGraphicsExtractor g,int originX,int originY,int width,int height,MenuBackgroundOptions options){
        var c=options.character;if(!c.enabled||width<24||height<40)return;
        var p=CharacterPlacement.of(c,width,height);int cx=originX+p.centerX(),feet=originY+p.feetY();
        g.enableScissor(originX,originY,originX+width,originY+height);
        try{
            if(CinematicTerrain.usesIllustrated(options))SceneForegroundRenderer.draw(g,originX,originY,width,height,options);
            else if(!CinematicTerrain.usesBlocks(options))ScenePlatformRenderer.draw(g,cx,feet,originY+height,p.platformSize(),options.scene,c);
            AvatarRenderState state=state(c,options);
            float size=p.bodyHeight()/1.8f;
            var tilt=new Quaternionf().rotateX((float)Math.toRadians(c.tilt));
            var rotation=new Quaternionf().rotateZ((float)Math.PI).mul(tilt);
            int half=Math.max(8,(int)(p.bodyHeight()*.53));
            g.entity(state,size,new Vector3f(0,.9f,0),rotation,tilt,cx-half,feet-p.bodyHeight()-4,cx+half,feet+4);
            g.nextStratum();
            if(c.matchLighting){int color=SceneLighting.of(options.scene).color();g.fillGradient(originX,originY,originX+width,originY+height,0x00000000|color,0x18000000|color);}
            if(!CinematicTerrain.usesBlocks(options)&&!CinematicTerrain.usesIllustrated(options)&&c.base!=CharacterComposition.Base.BLOCK&&(c.platform==CharacterComposition.Platform.GRASS||c.platform==CharacterComposition.Platform.MOSS))for(int i=0;i<7;i++){int x=cx-18+i*6,y=feet+3;g.fill(x,y-3-i%3,x+1,y+1,0xA05C784B);g.fill(x+1,y-1,x+3,y+1,0xA06C8654);}
            if(c.nameplate){var name=Component.literal(Minecraft.getInstance().getUser().getName());float scale=Math.min(Math.max(.7f,p.bodyHeight()/125f),(width-8)/(float)Math.max(1,Minecraft.getInstance().font.width(name)+16));int labelWidth=Math.round(Minecraft.getInstance().font.width(name)*scale),labelX=Math.clamp(cx-labelWidth/2,originX+4,Math.max(originX+4,originX+width-labelWidth-4));g.pose().pushMatrix();g.pose().translate(labelX,Math.max(originY+4,feet-p.bodyHeight()-Math.round(13*scale)));g.pose().scale(scale);try{g.text(Minecraft.getInstance().font,name,0,0,0xFFFFFFFF,true);}finally{g.pose().popMatrix();}}
            caption(g,originX,originY,width,height,c);
        }finally{g.disableScissor();}
    }
    private static AvatarRenderState state(CharacterComposition options,MenuBackgroundOptions background){
        var client=Minecraft.getInstance();AvatarRenderState state;
        if(client.player!=null){var renderer=client.getEntityRenderDispatcher().getRenderer(client.player);state=(AvatarRenderState)renderer.createRenderState(client.player,1);}
        else{
            state=new AvatarRenderState();var account=client.getGameProfile();
            if(!account.id().equals(profile)){profile=account.id();cachedSkin=client.getSkinManager().createLookup(account,false);}
            state.skin=cachedSkin==null?DefaultPlayerSkin.get(account):cachedSkin.get();if(state.skin==null)state.skin=DefaultPlayerSkin.get(account);
        }
        state.entityType=EntityTypes.PLAYER;state.boundingBoxWidth=.6f;state.boundingBoxHeight=1.8f;state.eyeHeight=1.62f;state.scale=state.ageScale=1;
        state.pose=Pose.STANDING;state.bodyRot=options.view.yaw+options.turn;state.yRot=0;state.xRot=-3;
        state.walkAnimationPos=state.walkAnimationSpeed=state.deathTime=state.swimAmount=state.fallFlyingTimeInTicks=state.swingAnimation=0;
        state.isCrouching=state.isFallFlying=state.isVisuallySwimming=state.isPassenger=state.isUsingItem=state.isInvisible=state.isInvisibleToPlayer=state.isUpsideDown=state.isFullyFrozen=state.isAutoSpinAttack=state.isInWater=state.isBaby=state.hasRedOverlay=state.isSpectator=false;
        state.ageInTicks=background.animations?(float)((System.nanoTime()/1e9)%4096*20):0;state.displayFireAnimation=false;state.outlineColor=0;state.nameTag=null;state.scoreText=null;state.leashStates=null;state.shadowPieces.clear();
        state.headEquipment=state.chestEquipment=state.legsEquipment=state.feetEquipment=state.rightHandItemStack=state.leftHandItemStack=ItemStack.EMPTY;
        state.headItem.clear();state.heldOnHead.clear();state.rightHandItemState.clear();state.leftHandItemState.clear();state.wornHeadType=null;state.wornHeadProfile=null;
        state.mainArm=HumanoidArm.RIGHT;state.rightArmPose=state.leftArmPose=HumanoidModel.ArmPose.EMPTY;state.parrotOnLeftShoulder=state.parrotOnRightShoulder=null;
        state.showHat=state.showJacket=state.showLeftPants=state.showRightPants=state.showLeftSleeve=state.showRightSleeve=true;
        state.showCape=options.cape;state.capeFlap=state.capeLean=state.capeLean2=0;state.shouldApplyFlyingYRot=false;state.arrowCount=state.stingerCount=0;var light=SceneLighting.of(background.scene);state.lightCoords=options.matchLighting&&client.level!=null?(light.block()<<4|light.sky()<<20):0xF000F0;return state;
    }
    private static Component component(String value,int font){return switch(font){case 0->MenuText.text(value,true);case 1->MenuText.text(value,false);case 2->TextFonts.text(value,3);default->Component.literal(value);};}
    private static void caption(GuiGraphicsExtractor g,int ox,int oy,int w,int h,CharacterComposition c){
        String player=Minecraft.getInstance().getUser().getName();String[] title=c.title.replace("{player}",player).split("\n",-1),subtitle=c.subtitle.replace("{player}",player).split("\n",-1);
        float main=2.3f*c.textSize/100f,sub=1.05f*c.textSize/100f;int margin=Math.max(12,w/20),available=w-margin*2;
        var font=Minecraft.getInstance().font;int widest=1;for(var line:title)widest=Math.max(widest,font.width(component(line,c.font)));main=Math.min(main,available/(float)widest);
        widest=1;for(var line:subtitle)widest=Math.max(widest,font.width(component(line,c.font)));sub=Math.min(sub,available/(float)widest);
        int th=c.title.isEmpty()?0:(int)(title.length*14*main),sh=c.subtitle.isEmpty()?0:(int)(subtitle.length*14*sub),total=th+sh+(th>0&&sh>0?8:0);
        float fit=Math.min(1,Math.max(1,h*.48f)/Math.max(1,total));main*=fit;sub*=fit;th=(int)(th*fit);sh=(int)(sh*fit);total=th+sh+(th>0&&sh>0?8:0);
        int y=oy+(c.caption.ordinal()<3?Math.max(12,h/10):Math.max(8,h-h/12-total));
        if(!c.title.isEmpty())for(String line:title){line(g,component(line,c.font),ox,y,w,margin,main,c.titleColor,c.caption.ordinal()%3,c.textShadow);y+=(int)(14*main);}
        if(!c.subtitle.isEmpty()){if(th>0)y+=8;for(String line:subtitle){line(g,component(line,c.font),ox,y,w,margin,sub,c.subtitleColor,c.caption.ordinal()%3,c.textShadow);y+=(int)(14*sub);}}
    }
    private static void line(GuiGraphicsExtractor g,Component text,int ox,int y,int w,int margin,float scale,int color,int align,boolean shadow){int width=Math.round(Minecraft.getInstance().font.width(text)*scale),x=align==0?ox+margin:align==1?ox+(w-width)/2:ox+w-margin-width;g.pose().pushMatrix();g.pose().translate(x,y);g.pose().scale(scale);try{g.text(Minecraft.getInstance().font,text,0,0,0xFF000000|color,shadow);}finally{g.pose().popMatrix();}}
}
