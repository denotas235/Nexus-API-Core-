package com.maliopt.mixin;

import com.maliopt.world.WorldCache;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.BuiltChunk;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SectionBuilder.class)
public class MixinSectionBuilder {

    @Inject(method = "build", at = @At("HEAD"), cancellable = true, require = 1)
    private void onBuild(Map<RenderLayer, BuiltBuffer> buffers,
                         BuiltChunk builtChunk,
                         Box box,
                         CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
        // Se o chunk já está em cache, reutiliza
        long chunkKey = builtChunk.getOrigin().asLong();
        if (WorldCache.getChunkBuffer(chunkKey) >= 0) {
            SectionBuilder.RenderData data = new SectionBuilder.RenderData();
            cir.setReturnValue(data);
        }
    }
}
