package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;

/**
 * ASTCTextureLoader — faz upload de dados ASTC para a GPU
 * usando glCompressedTexImage2D com o formato correcto.
 */
public final class ASTCTextureLoader {

    private static boolean astcAvailable = false;

    // Formatos ASTC (valores hex dos enums GL)
    private static final int[] BLOCK_FORMATS = {
        0x93B0, // 4x4
        0x93B1, // 5x4
        0x93B2, // 5x5
        0x93B3, // 6x5
        0x93B4, // 6x6
        0x93B5, // 8x5
        0x93B6, // 8x6
        0x93B7, // 8x8
        0x93B8, // 10x5
        0x93B9, // 10x6
        0x93BA, // 10x8
        0x93BB, // 10x10
        0x93BC, // 12x10
        0x93BD  // 12x12
    };

    private ASTCTextureLoader() {}

    public static void init() {
        astcAvailable = ExtensionActivator.hasAstcLdr;
        MaliOptMod.LOGGER.info("[ASTCLoader] ASTC LDR: {}", astcAvailable ? "✅" : "❌");
    }

    public static boolean isAvailable() {
        return astcAvailable;
    }

    public static int upload(int width, int height, int blockX, int blockY, byte[] compressedData) {
        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);

        int format = getBlockFormat(blockX, blockY);
        ByteBuffer buf = ByteBuffer.allocateDirect(compressedData.length);
        buf.put(compressedData).flip();

        GL30.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, 0, format, width, height, 0, buf);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        MaliOptMod.LOGGER.debug("[ASTCLoader] Textura ASTC carregada: {}x{} (block={}x{}, handle={})",
            width, height, blockX, blockY, tex);
        return tex;
    }

    private static int getBlockFormat(int bx, int by) {
        switch ((bx << 8) | by) {
            case 0x0404: return BLOCK_FORMATS[0];  // 4x4
            case 0x0504: return BLOCK_FORMATS[1];  // 5x4
            case 0x0505: return BLOCK_FORMATS[2];  // 5x5
            case 0x0605: return BLOCK_FORMATS[3];  // 6x5
            case 0x0606: return BLOCK_FORMATS[4];  // 6x6
            case 0x0805: return BLOCK_FORMATS[5];  // 8x5
            case 0x0806: return BLOCK_FORMATS[6];  // 8x6
            case 0x0808: return BLOCK_FORMATS[7];  // 8x8
            default:     return BLOCK_FORMATS[4];  // fallback 6x6
        }
    }
}
