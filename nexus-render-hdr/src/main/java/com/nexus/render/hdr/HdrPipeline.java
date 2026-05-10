package com.nexus.render.hdr;

import org.lwjgl.opengl.*;

public class HdrPipeline {
    private static boolean sRGBWriteControl = false;
    private static boolean anisotropicFilter = false;
    private static float maxAnisotropy = 1.0f;
    private static int tonemapProgram = 0;
    private static int quadVao = 0;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        System.out.println("[HDR] Extensions: " + (extensions != null ? "OK" : "NULL"));

        sRGBWriteControl = extensions != null && extensions.contains("GL_EXT_sRGB_write_control");
        if (sRGBWriteControl) {
            GL11.glEnable(0x8BF2);
            System.out.println("[HDR] sRGB framebuffer ENABLED");
        } else {
            System.out.println("[HDR] sRGB framebuffer NOT available");
        }

        anisotropicFilter = extensions != null && extensions.contains("GL_EXT_texture_filter_anisotropic");
        if (anisotropicFilter) {
            float[] max = new float[1];
            try {
                GL11.glGetFloatv(0x84FF, max);
                maxAnisotropy = max[0];
            } catch (Exception e) {
                maxAnisotropy = 16.0f;
            }
            System.out.println("[HDR] Anisotropic available (max " + maxAnisotropy + "x)");
        } else {
            System.out.println("[HDR] Anisotropic NOT available");
        }

        TonemappingShader.compile();
        System.out.println("[HDR] Pipeline initialized.");
    }

    public static void applyAnisotropic(int textureId) {
        if (!anisotropicFilter || textureId == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 0x84FE, maxAnisotropy);
    }

    public static boolean isReady() { return initialized; }
    public static boolean hasSRGB() { return sRGBWriteControl; }
}
