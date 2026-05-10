package com.nexus.textures.mixin;

import com.nexus.textures.TexturePathTracker;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

@Mixin(TextureManager.class)
public class TextureManagerMixin {
    @Inject(method = "registerTexture(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/texture/AbstractTexture;)V",
            at = @At("HEAD"))
    private void onRegisterTexture(Identifier id, Object texture, CallbackInfo ci) {
        TexturePathTracker.setCurrentPath(id != null ? id.toString() : null);
    }
}
