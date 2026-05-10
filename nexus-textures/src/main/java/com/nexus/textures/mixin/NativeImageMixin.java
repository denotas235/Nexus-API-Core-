package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(NativeImage.class)
public class NativeImageMixin {

    @Inject(method = "upload(IIIIIIIZZZZ)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void onUpload(int level, int xOffset, int yOffset,
                          int skipPixels, int skipRows,
                          int width, int height,
                          boolean blur, boolean mipmap,
                          boolean close, boolean linear,
                          CallbackInfo ci) {
        if (level != 0 || width < 4 || height < 4) return;

        String path = TexturePathTracker.getCurrentPath();
        if (path == null) return;

        NativeImage self = (NativeImage)(Object) this;

        // Verificar cache primeiro
        byte[] astc = ASTCCache.get(path);
        if (astc != null) {
            uploadASTC(astc, width, height);
            ci.cancel();
            return;
        }

        // Extrair RGBA
        byte[] rgba = new byte[width * height * 4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = self.getColor(x, y);
                int idx = (y * width + x) * 4;
                rgba[idx]     = (byte)((pixel >> 16) & 0xFF);
                rgba[idx + 1] = (byte)((pixel >>  8) & 0xFF);
                rgba[idx + 2] = (byte)( pixel        & 0xFF);
                rgba[idx + 3] = (byte)((pixel >> 24) & 0xFF);
            }
        }

        // Comprimir
        ASTCTextureCategory cat = ASTCTextureCategory.fromPath(path);
        byte[] compressed = ASTCEncoder.compress(rgba, width, height, cat, false); // fast
        if (compressed != null && compressed.length >= 16) {
            ASTCCache.put(path, compressed);
            uploadASTC(compressed, width, height);
            ci.cancel();
            // Agendar recompressão de alta qualidade em background
            BackgroundRecompressor.schedule(path, rgba, width, height, cat);
        }
        // Se falhou compressão, deixa o upload vanilla seguir
    }

    private void uploadASTC(byte[] astcData, int width, int height) {
        if (astcData.length < 16) return;
        int blockX = astcData[7] & 0xFF;
        int blockY = astcData[8] & 0xFF;
        int glFormat = ASTCTextureCategory.fromPath("").getGLInternalFormat(); // fallback
        // Usar o formato real da categoria seria melhor, mas para já usamos 4x4 SRGB
        if (blockX == 5 && blockY == 5) glFormat = 0x93B3;
        else if (blockX == 8 && blockY == 8) glFormat = 0x93B7;
        else if (blockX == 10 && blockY == 10) glFormat = 0x93BD;
        else if (blockX == 12 && blockY == 12) glFormat = 0x93BF;
        else glFormat = 0x93B0; // 4x4

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL13.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, 0, glFormat,
                width, height, 0,
                ByteBuffer.wrap(astcData, 16, astcData.length - 16));
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        System.out.println("[NexusASTC] Uploaded compressed texture: " + TexturePathTracker.getCurrentPath());
    }
}
