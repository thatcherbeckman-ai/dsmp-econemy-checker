package dev.dsmp.economy.ui;

import dev.dsmp.economy.menubackground.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Decorative own-skin preview outside the menu. It has no input target or live-player mutations. */
public final class MenuAvatarRenderer {
    private MenuAvatarRenderer(){}
    public static void draw(GuiGraphicsExtractor g,MenuWindow window,int mouseX,int mouseY){
        var options=MenuBackgrounds.visible();if(!options.miniAvatar)return;
        var b=MenuDisplay.avatar(window.viewportWidth(),window.viewportHeight(),window.x()+window.width()*window.scale(),window.y()+window.height()*window.scale(),options.avatarSize);
        if(b.height()<20)return;
        var look=MenuDisplay.look(b,mouseX,mouseY,options.avatarFollowsMouse);var state=CharacterSceneRenderer.mouseAvatar(look);
        g.nextStratum();UiSurfaces.fill(g,b.x()+b.width()/5,b.bottom()-3,Math.max(2,b.width()*3/5),3,0x35000000);
        GuiEntities.entity(g,state,Math.max(1,(b.height()-6)/1.8f),new Vector3f(0,.9f,0),new Quaternionf().rotateZ((float)Math.PI),new Quaternionf(),b.x(),b.y(),b.right(),b.bottom());
    }
}
