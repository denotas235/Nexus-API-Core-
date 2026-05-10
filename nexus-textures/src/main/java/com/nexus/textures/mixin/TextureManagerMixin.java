package com.nexus.textures.mixin;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Inject(method = "registerTexture", at = @At("HEAD"))
    private void onRegisterTexture(Identifier id, AbstractTexture texture, CallbackInfo ci) {
        // placeholder – a lógica ASTC será ligada aqui
    }
}
