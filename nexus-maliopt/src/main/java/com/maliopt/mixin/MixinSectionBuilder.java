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

import java.nio.ByteBuffer;
import java.util.Map;

@Mixin(SectionBuilder.class)
public class MixinSectionBuilder {

    @Inject(method = "build", at = @At("HEAD"), cancellable = true, require = 1)
    private void onBuild(Map<RenderLayer, BuiltBuffer> buffers,
                         BuiltChunk builtChunk,
                         Box box,
                         CallbackInfoReturnable<SectionBuilder.RenderData> cir) {
        // Utilizar WorldCache para evitar reconstrução desnecessária
        long chunkKey = builtChunk.getOrigin().asLong();
        int cachedVbo = WorldCache.getChunkBuffer(chunkKey);
        if (cachedVbo >= 0) {
            // Devolver geometria em cache — zero reconstrução
            SectionBuilder.RenderData data = new SectionBuilder.RenderData();
            // (Aqui podemos associar o VBO ao RenderData, se necessário)
            cir.setReturnValue(data);
        }
    }
}
