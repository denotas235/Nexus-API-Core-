package com.nexus.nefu.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexus.nefu.BatchManager;
import com.nexus.nefu.NefuCoreEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts RenderSystem draw calls and redirects them through the NEFU
 * BatchManager for grouping and native dispatch.
 *
 * remap=false  — bypasses Loom's remapper so no compile-time error occurs if
 *                the target method is renamed between MC versions.
 * require=0    — silently skips injection instead of crashing if the method
 *                signature is not found at runtime.
 *
 * NOTE: If this injection stops working after an MC update, locate the new
 * drawArrays intermediary name at https://yarn.modmuss50.me and update the
 * method descriptor below.
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(
        method    = "drawArrays(III)V",
        at        = @At("HEAD"),
        cancellable = true,
        remap     = false,
        require   = 0
    )
    private static void onDrawArrays(int mode, int first, int count,
                                     CallbackInfo ci) {
        if (NefuCoreEngine.isActive()) {
            BatchManager.queue(mode, first, count);
            ci.cancel();
        }
    }
}
