package com.nexus.render.hdr;

import org.lwjgl.opengl.*;

public class HdrPipeline {
    private static boolean sRGBWriteControl = false;
    private static boolean anisotropicFilter = false;
    private static float maxAnisotropy = 16.0f; // fallback seguro
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        // Verificar as extensões directamente nas strings do driver nativo
        String driverExtensions = getMaliDriverExtensions();
        
        sRGBWriteControl = driverExtensions.contains("GL_EXT_sRGB_write_control");
        anisotropicFilter = driverExtensions.contains("GL_EXT_texture_filter_anisotropic");

        System.out.println("[HDR] Driver Mali: sRGB " + (sRGBWriteControl ? "✅" : "❌") +
                           " | Anisotropic " + (anisotropicFilter ? "✅" : "❌"));

        // Activar sRGB no framebuffer se disponível
        if (sRGBWriteControl) {
            try {
                GL11.glEnable(0x8BF2); // GL_FRAMEBUFFER_SRGB_EXT
                System.out.println("[HDR] sRGB framebuffer ENABLED via driver");
            } catch (Exception e) {
                System.out.println("[HDR] Failed to enable sRGB: " + e.getMessage());
            }
        }

        // Compilar shader de tonemapping (versão compatível com GLES 3.0)
        TonemappingShader.compile();
        System.out.println("[HDR] Pipeline initialized.");
    }

    /**
     * Lê as strings do driver Mali directamente, sem depender do OpenLTW.
     */
    private static String getMaliDriverExtensions() {
        try {
            // Caminho do driver Mali no sistema
            String libPath = "/vendor/lib64/egl/libGLES_mali.so";
            java.io.File f = new java.io.File(libPath);
            if (!f.exists()) {
                System.out.println("[HDR] libGLES_mali.so not found at " + libPath);
                return "";
            }
            ProcessBuilder pb = new ProcessBuilder("strings", libPath);
            Process p = pb.start();
            java.io.InputStream is = p.getInputStream();
            java.util.Scanner scanner = new java.util.Scanner(is).useDelimiter("\\A");
            String output = scanner.hasNext() ? scanner.next() : "";
            p.waitFor();
            return output;
        } catch (Exception e) {
            System.out.println("[HDR] Failed to read driver strings: " + e.getMessage());
            return "";
        }
    }

    public static void applyAnisotropic(int textureId) {
        if (!anisotropicFilter || textureId == 0) return;
        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            // Usa a constante real da extensão para max anisotropy
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 0x84FE, Math.min(maxAnisotropy, 16.0f));
        } catch (Exception ignored) {}
    }

    public static boolean isReady() { return initialized; }
    public static boolean hasSRGB() { return sRGBWriteControl; }
}
