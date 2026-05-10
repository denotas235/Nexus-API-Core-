package com.nexus.textures;

import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;

public class ASTCUploader {

    private static final int GL_TEXTURE_ASTC_DECODE_PRECISION_EXT = 0x8F69;
    private static final int GL_RGBA16F                            = 0x881A;

    public static void upload(
        int textureId,
        int width,
        int height,
        ASTCTextureCategory category,
        byte[] astcData
    ) {
        if (astcData == null || astcData.length < 16) return;
        if (textureId <= 0) return;
        if (width <= 0 || height <= 0) return;

        // Guarda estado anterior
        int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int prevPBO     = GL11.glGetInteger(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING);

        int pbo = -1;
        ByteBuffer buffer = null;

        try {
            // Bind da textura correcta
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            // Cria PBO
            pbo    = GL15.glGenBuffers();
            buffer = MemoryUtil.memAlloc(astcData.length);
            buffer.put(astcData).flip();

            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pbo);
            GL15.glBufferData(GL21.GL_PIXEL_UNPACK_BUFFER, buffer, GL15.GL_STATIC_DRAW);

            // Decode mode FP16 — exclusivo Mali
            applyDecodeMode();

            // Upload comprimido via PBO
            GL13.glCompressedTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                category.getGLInternalFormat(),
                width,
                height,
                0,
                astcData.length,
                0L
            );

            // Gera mipmaps
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            // Filtragem anisotrópica máxima
            applyAnisotropic();

        } catch (Exception e) {
            System.err.println("[NexusASTC] Upload erro: " + e.getMessage());
        } finally {
            // Liberta PBO
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, prevPBO);
            if (pbo > 0) GL15.glDeleteBuffers(pbo);
            if (buffer != null) MemoryUtil.memFree(buffer);

            // Restaura textura anterior
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
        }
    }

    private static void applyDecodeMode() {
        try {
            GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL_TEXTURE_ASTC_DECODE_PRECISION_EXT,
                GL_RGBA16F
            );
        } catch (Exception ignored) {}
    }

    private static void applyAnisotropic() {
        try {
            float max = GL11.glGetFloat(
                EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
            );
            GL11.glTexParameterf(
                GL11.GL_TEXTURE_2D,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                max
            );
        } catch (Exception ignored) {}
    }
}
