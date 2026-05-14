package com.nexus.nefu.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.nexus.nefu.BatchManager;
import com.nexus.nefu.NefuCoreEngine;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts RenderSystem.drawArrays(VertexFormat.DrawMode, int, int) in MC 1.21.1.
 * remap=true lets Fabric Loom resolve the yarn-mapped descriptor automatically.
 */
@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(
        method = "drawArrays(Lnet/minecraft/client/render/VertexFormat$DrawMode;II)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = true
    )
    private static void onDrawArrays(VertexFormat.DrawMode mode, int first, int count, CallbackInfo ci) {
        if (NefuCoreEngine.isActive()) {
            BatchManager.queue(mode.glMode, first, count);
            ci.cancel();
        }
    }
}
