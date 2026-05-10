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
            String path = id.getNamespace() + "/" + id.getPath();

            // Verifica vanilla pré-comprimida primeiro
            if (ASTCVanillaLoader.hasPrecompressed(path)) {
                ASTCUploadQueue.registerPath(path, null);
                return;
            }

            // Tenta obter NativeImage da textura
            if (texture instanceof NativeImageBackedTexture nib) {
                var image = nib.getImage();
                if (image != null) {
                    ASTCUploadQueue.registerPath(path, image);
                }
            }

        } catch (Exception e) {
            System.err.println("[NexusASTC] TextureManagerMixin erro: " + e.getMessage());
        }
    }

    @Inject(
        method = "bindTexture",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onBindTexture(
        Identifier id,
        CallbackInfo ci
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;
        if (id == null)                        return;

        try {
            String path = id.getNamespace() + "/" + id.getPath();

            // Verifica vanilla pré-comprimida
            byte[] vanillaAstc = ASTCVanillaLoader.loadPrecompressed(path);
            if (vanillaAstc != null) {
                ASTCTextureCategory category = ASTCTextureCategory.fromPath(path);
                // Obtém GL id da textura actual
                int glId = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
                if (glId > 0) {
                    // Precisamos largura/altura — lê do header ASTC
                    int[] dims = readASTCDimensions(vanillaAstc);
                    ASTCUploader.upload(glId, dims[0], dims[1], category, vanillaAstc);
                    ci.cancel();
                    return;
                }
            }

            // Verifica fila de upload runtime
            ASTCUploadQueue.Entry entry = ASTCUploadQueue.poll(path);
            if (entry == null) return;

            byte[] astcData = ASTCCache.load(entry.cachePath);
            if (astcData == null) return;

            int glId = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
            if (glId <= 0) return;

            ASTCUploader.upload(glId, entry.width, entry.height, entry.category, astcData);
            ci.cancel();

        } catch (Exception e) {
            System.err.println("[NexusASTC] onBindTexture erro: " + e.getMessage());
            // Não cancela — deixa Minecraft fazer bind normal
        }
    }

    // Lê largura/altura do header ASTC (bytes 7-12)
    private static int[] readASTCDimensions(byte[] astc) {
        if (astc.length < 16) return new int[]{16, 16};
        int width  = (astc[7]  & 0xFF) | ((astc[8]  & 0xFF) << 8) | ((astc[9]  & 0xFF) << 16);
        int height = (astc[10] & 0xFF) | ((astc[11] & 0xFF) << 8) | ((astc[12] & 0xFF) << 16);
        return new int[]{Math.max(1, width), Math.max(1, height)};
    }
}
