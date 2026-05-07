package com.maliopt.mixin;

import com.maliopt.world.MeshDiffEngine;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
// Temporariamente desativado — aguardar nome correto do BuiltBuffer
// public class MixinVertexBuffer {

    @Inject(method = "upload", at = @At("HEAD"), cancellable = true, require = 1)
    private void onUpload(BufferBuilder.BuiltBuffer builtBuffer, CallbackInfo ci) {
        // Usar MeshDiffEngine para upload parcial
        // (O chunkKey e o vbo são geridos pelo ChunkBuilder; aqui apenas fazemos o upload optimizado)
        // ci.cancel(); // se quisermos substituir completamente o upload vanilla
    }
}
