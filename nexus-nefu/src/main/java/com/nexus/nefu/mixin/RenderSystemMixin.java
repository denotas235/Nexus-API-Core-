package com.nexus.nefu.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexus.nefu.BatchManager;
import com.nexus.nefu.NefuCoreEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "method_43479", at = @At("HEAD"), cancellable = true, remap = false)
    private static void onDrawArrays(int mode, int first, int count, CallbackInfo ci) {
        if (NefuCoreEngine.isActive()) {
            BatchManager.queue(mode, first, count);
            ci.cancel();
        }
    }
}
