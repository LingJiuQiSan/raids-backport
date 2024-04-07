package net.smileycorp.raids.mixin;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelIllager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderVindicator;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.entity.monster.EntityMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderVindicator.class)
public abstract class MixinRenderVindicator extends RenderLiving<EntityMob> {
    
    public MixinRenderVindicator(RenderManager rendermanagerIn, ModelBase modelbaseIn, float shadowsizeIn) {
        super(rendermanagerIn, modelbaseIn, shadowsizeIn);
    }
    
    @Inject(at =@At("TAIL"), method = "<init>")
    public void init(RenderManager renderManager, CallbackInfo callback)  {
        addLayer(new LayerCustomHead(((ModelIllager)getMainModel()).head));
    }
    
}
