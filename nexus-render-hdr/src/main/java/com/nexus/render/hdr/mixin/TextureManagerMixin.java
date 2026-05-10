package com.nexus.render.hdr.mixin;

import com.nexus.render.hdr.HdrPipeline;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Inject(method = "bindTexture", at = @At("TAIL"), require = 0)
    private void onBindTexture(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        if (!HdrPipeline.isReady() || texture == null) return;
        int glId = texture.getGlId();
        if (glId > 0) HdrPipeline.applyAnisotropic(glId);
    }
}
