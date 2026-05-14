package com.nexus.nefu.mixin;

import com.nexus.nefu.BatchManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flushes all pending NEFU batched draw calls after WorldRenderer.renderWorld completes.
 * Target: WorldRenderer.renderWorld(RenderTickCounter) -- MC 1.21.1 yarn-mapped name.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderWorld", at = @At("RETURN"), remap = true)
    private void afterRenderWorld(RenderTickCounter tickCounter, CallbackInfo ci) {
        BatchManager.flush();
    }
}
