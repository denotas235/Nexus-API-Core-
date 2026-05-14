package com.nexus.nefu.mixin;

import com.nexus.nefu.BatchManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "method_34853", at = @At("RETURN"), remap = false)
    private void afterRender(CallbackInfo ci) {
        BatchManager.flush();
    }
}
