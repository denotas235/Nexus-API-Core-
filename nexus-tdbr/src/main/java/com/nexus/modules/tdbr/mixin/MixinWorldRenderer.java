package com.nexus.modules.tdbr.mixin;

import com.nexus.modules.tdbr.PLSManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(CallbackInfo ci) {
        PLSManager.INSTANCE.beginFrame();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(CallbackInfo ci) {
        PLSManager.INSTANCE.endFrame();
    }
}
