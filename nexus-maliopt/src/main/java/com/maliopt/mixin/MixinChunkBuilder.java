package com.maliopt.mixin;

import com.maliopt.geometry.FrustumCuller;
import com.maliopt.geometry.OcclusionCuller;
import com.maliopt.geometry.GreedyMesher;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkBuilder.class)
public class MixinChunkBuilder {

    @Inject(method = "build", at = @At("HEAD"))
    private void onBuildStart(CallbackInfo ci) {
        FrustumCuller.isEnabled();
        OcclusionCuller.isEnabled();
        GreedyMesher.isEnabled();
    }
}
