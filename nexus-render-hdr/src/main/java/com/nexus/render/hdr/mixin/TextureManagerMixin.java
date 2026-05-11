package com.nexus.render.hdr.mixin;

import com.nexus.render.hdr.HdrPipeline;
import net.minecraft.client.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Aplica filtragem anisotropica (AF) uma vez por objeto de textura,
 * imediatamente apos o bind. O campo nexusAfApplied garante que a
 * chamada GL e feita apenas no primeiro bind de cada textura.
 */
@Mixin(AbstractTexture.class)
public class TextureManagerMixin {

    private boolean nexusAfApplied = false;

    @Inject(method = "bindTexture", at = @At("TAIL"), require = 0)
    private void onBindTexture(CallbackInfo ci) {
        if (nexusAfApplied || !HdrPipeline.hasAnisotropic()) return;
        nexusAfApplied = true;
        HdrPipeline.applyAnisotropic();
    }
}