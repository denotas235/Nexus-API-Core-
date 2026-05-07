package com.maliopt.mixin;

import com.maliopt.geometry.FrustumCuller;
import com.maliopt.geometry.OcclusionCuller;
import com.maliopt.geometry.GreedyMesher;
import com.maliopt.geometry.MultiDrawManager;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegionBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkBuilder.class)
public class MixinChunkBuilder {

    @Inject(method = "rebuild", at = @At("HEAD"), require = 1)
    private void onRebuildStart(ChunkBuilder.BuiltChunk builtChunk,
                                ChunkRendererRegionBuilder regionBuilder,
                                CallbackInfo ci) {
        // Ativa as otimizações de geometria sempre que um chunk é reconstruído
        FrustumCuller.isEnabled();
        OcclusionCuller.isEnabled();
        GreedyMesher.isEnabled();
        MultiDrawManager.isEnabled();
    }
}
