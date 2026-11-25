package dev.dsmp.economy.mixin;

import dev.dsmp.economy.inventory.*;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Replace only the survival inventory backdrop. Vanilla owns every item/input operation. */
@Mixin(InventoryScreen.class)
public abstract class InventoryScreenThemeMixin {
    @Redirect(method="extractBackground",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"),require=1)
    private void economy$inventoryBackground(GuiGraphicsExtractor g,RenderPipeline pipeline,Identifier texture,int x,int y,float u,float v,int width,int height,int textureWidth,int textureHeight){
        var options=InventoryThemes.current();
        if(options.theme()==InventoryTheme.VANILLA||width!=HolidayScene.WIDTH||height!=HolidayScene.HEIGHT)g.blit(pipeline,texture,x,y,u,v,width,height,textureWidth,textureHeight);
        else InventoryPainter.background(g,x,y,options);
    }
    @ModifyConstant(method="extractLabels",constant=@Constant(intValue=-12566464),require=1)
    private int economy$readableLabel(int original){return InventoryThemes.current().theme()==InventoryTheme.VANILLA?original:0xFFE9EDF6;}
}
