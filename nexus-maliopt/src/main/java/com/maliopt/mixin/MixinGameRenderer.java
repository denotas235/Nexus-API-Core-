package com.maliopt.mixin;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.pipeline.MaliPipelineOptimizer;
import com.maliopt.pipeline.ShaderCacheManager;
import com.maliopt.pipeline.TileBasedOptimizer;
import net.minecraft.class_757;
import net.minecraft.class_9779;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(class_757.class)
public class MixinGameRenderer {

    private static boolean lateInitDone = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void maliopt$onRenderHead(class_9779 tickCounter,
                                      boolean tick,
                                      CallbackInfo ci) {
        if (!GPUDetector.isMaliGPU()) return;

        if (!lateInitDone && !MaliPipelineOptimizer.isInitialized()) {
            MaliOptMod.LOGGER.info("[MaliOpt] Init tardio via render hook");
            MaliPipelineOptimizer.init();
            ShaderCacheManager.init();
            lateInitDone = true;
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void maliopt$onRenderTail(class_9779 tickCounter,
                                      boolean tick,
                                      CallbackInfo ci) {
        if (!GPUDetector.isMaliGPU()) return;
        // Fase 2 — delega para TileBasedOptimizer
        MaliPipelineOptimizer.onFrameEnd();
    }
}
