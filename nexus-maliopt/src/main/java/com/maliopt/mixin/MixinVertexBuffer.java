package com.maliopt.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.gl.VertexBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VertexBuffer.class)
// public class MixinVertexBuffer {

    @Inject(method = "upload", at = @At("HEAD"), cancellable = true)
    private void onUpload(BufferBuilder.BuiltBuffer builtBuffer, CallbackInfo ci) {
        // Lógica de Partial Geometry Upload para Mali (em breve)
    }
}
