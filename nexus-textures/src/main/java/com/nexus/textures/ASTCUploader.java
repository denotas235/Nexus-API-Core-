package com.nexus.textures;

import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import org.lwjgl.system.MemoryUtil;

public class ASTCUploader {

    // GL constantes ASTC
    private static final int GL_TEXTURE_ASTC_DECODE_PRECISION_EXT = 0x8F69;
    private static final int GL_RGBA16F                            = 0x881A;

    // Upload ASTC comprimido via PBO para zero stutter
    public static void upload(
        int textureId,
        int width,
        int height,
        ASTCTextureCategory category,
        byte[] astcData
    ) {
        if (astcData == null || astcData.length == 0) return;

        // Cria PBO
        int pbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pbo);

        // Aloca buffer nativo e copia dados ASTC
        ByteBuffer buffer = MemoryUtil.memAlloc(astcData.length);
        try {
            buffer.put(astcData).flip();

            // Upload dados para o PBO (assíncrono)
            GL15.glBufferData(
                GL21.GL_PIXEL_UNPACK_BUFFER,
                buffer,
                GL15.GL_STATIC_DRAW
            );

            // Bind textura e faz upload comprimido
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

            // Aplica decode mode FP16 — exclusivo Mali
            applyDecodeMode();

            // Upload comprimido — offset 0 no PBO
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

        } finally {
            MemoryUtil.memFree(buffer);
            GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
            GL15.glDeleteBuffers(pbo);
        }
    }

    // Decode mode FP16 — usa GL_EXT_texture_compression_astc_decode_mode
    // Diz ao Mali para decodificar ASTC em FP16 em vez de RGBA8
    // Resultado: mais precisão + mais rápido no TBDR
    private static void applyDecodeMode() {
        try {
            GL11.glTexParameteri(
                GL11.GL_TEXTURE_2D,
                GL_TEXTURE_ASTC_DECODE_PRECISION_EXT,
                GL_RGBA16F
            );
        } catch (Exception ignored) {
            // Silencioso se extensão não disponível
        }
    }

    // Filtragem anisotrópica — usa GL_EXT_texture_filter_anisotropic
    private static void applyAnisotropic() {
        try {
            float maxAniso = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
            GL11.glTexParameterf(
                GL11.GL_TEXTURE_2D,
                EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                maxAniso
            );
        } catch (Exception ignored) {
            // Silencioso se extensão não disponível
        }
    }
}
