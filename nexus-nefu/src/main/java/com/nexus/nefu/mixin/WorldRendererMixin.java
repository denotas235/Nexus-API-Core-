package com.nexus.nefu.mixin;

import com.nexus.nefu.BatchManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Flushes all pending NEFU batched draw calls at the end of each rendered frame.
 *
 * Target: WorldRenderer.renderWorld (MC 1.21.1 yarn-mapped name).
 * require=0: silently skips if the method is not found (version safety).
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(method = "renderWorld", at = @At("RETURN"), remap = true, require = 0)
    private void afterRenderWorld(CallbackInfo ci) {
        BatchManager.flush();
    }
}
