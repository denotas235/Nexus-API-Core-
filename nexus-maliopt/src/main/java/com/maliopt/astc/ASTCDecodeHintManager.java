package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;
import org.lwjgl.opengl.GL11;

/**
 * ASTCDecodeHintManager — configura decodificação on-chip FP16
 *
 * GL_TEXTURE_ASTC_DECODE_PRECISION_EXT = 0x8F69
 * GL_RGBA16F = 0x881A
 * GL_RGB9_E5 = 0x8C3D
 */
public final class ASTCDecodeHintManager {

    private static final int GL_TEXTURE_ASTC_DECODE_PRECISION_EXT = 0x8F69;
    private static final int GL_RGBA16F = 0x881A;
    private static final int GL_RGB9_E5 = 0x8C3D;

    private static boolean decodeModeAvailable = false;
    private static boolean decodeModeRGB9E5Available = false;

    private ASTCDecodeHintManager() {}

    public static void init() {
        decodeModeAvailable = ExtensionActivator.hasAstcLdr; // mesma extensão habilita decode mode
        decodeModeRGB9E5Available = ExtensionActivator.hasAstcHdr;
        MaliOptMod.LOGGER.info("[ASTCDecode] Decode FP16: {}, Decode RGB9E5: {}",
            decodeModeAvailable ? "✅" : "❌",
            decodeModeRGB9E5Available ? "✅" : "❌");
    }

    public static void apply(boolean isHDR) {
        if (!decodeModeAvailable) return;
        int precision = (isHDR && decodeModeRGB9E5Available) ? GL_RGB9_E5 : GL_RGBA16F;
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL_TEXTURE_ASTC_DECODE_PRECISION_EXT, precision);
    }

    public static void apply(int textureId, boolean isHDR) {
        if (!decodeModeAvailable) return;
        int prevTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        apply(isHDR);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTex);
    }
}
