package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    @Inject(
        method = "registerTexture",
        at = @At("HEAD")
    )
    private void onRegisterTexture(
        Identifier id,
        AbstractTexture texture,
        CallbackInfo ci
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;
        if (id == null || texture == null)     return;

        try {
            String path = id.getNamespace() + "/textures/" + id.getPath();

            // Vanilla pré-comprimida
            if (ASTCVanillaLoader.hasPrecompressed(path)) {
                ASTCUploadQueue.registerPath(path, null);
                return;
            }

            // Runtime — associa NativeImage ao path
            if (texture instanceof NativeImageBackedTexture nib) {
                var image = nib.getImage();
                if (image != null) {
                    ASTCUploadQueue.registerPath(path, image);
                }
            }

        } catch (Exception e) {
            System.err.println("[NexusASTC] onRegisterTexture erro: " + e.getMessage());
        }
    }
}
