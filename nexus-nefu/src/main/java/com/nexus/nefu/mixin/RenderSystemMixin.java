package com.nexus.nefu.mixin;

import com.nexus.nefu.BatchManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin {

    @Inject(method = "drawArrays", at = @At("HEAD"), cancellable = true)
    private static void onDrawArrays(int mode, int first, int count, CallbackInfo ci) {
        if (!BatchManager.isInitialized()) return;
        if (BatchManager.shouldBatch(mode, count)) {
            BatchManager.addToBatch(mode, first, count);
            ci.cancel();
        } else {
            BatchManager.flush();
            ci.cancel();
            org.lwjgl.opengl.GL11.glDrawArrays(mode, first, count);
        }
    }
}
