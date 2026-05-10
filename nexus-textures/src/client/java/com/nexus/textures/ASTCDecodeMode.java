package com.nexus.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GL;

public class ASTCDecodeMode {

    // Extensões necessárias
    private static boolean hasASTC_LDR       = false;
    private static boolean hasASTC_HDR       = false;
    private static boolean hasASTC_DecodeMode= false;
    private static boolean hasAnisotropic    = false;
    private static boolean hasPixelLocalStore= false;
    private static boolean hasFramebufferFetch=false;
    private static boolean initialized       = false;

    public static void init() {
        if (initialized) return;

        String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        if (extensions == null) extensions = "";

        hasASTC_LDR        = extensions.contains("GL_KHR_texture_compression_astc_ldr");
        hasASTC_HDR        = extensions.contains("GL_KHR_texture_compression_astc_hdr");
        hasASTC_DecodeMode = extensions.contains("GL_EXT_texture_compression_astc_decode_mode");
        hasAnisotropic     = extensions.contains("GL_EXT_texture_filter_anisotropic");
        hasPixelLocalStore = extensions.contains("GL_EXT_shader_pixel_local_storage");
        hasFramebufferFetch= extensions.contains("GL_ARM_shader_framebuffer_fetch");

        initialized = true;

        printReport();
    }

    private static void printReport() {
        System.out.println("[NexusASTC] ── Extensões Detectadas ──────────────────");
        System.out.println("[NexusASTC] ASTC LDR          : " + status(hasASTC_LDR));
        System.out.println("[NexusASTC] ASTC HDR          : " + status(hasASTC_HDR));
        System.out.println("[NexusASTC] ASTC Decode Mode  : " + status(hasASTC_DecodeMode));
        System.out.println("[NexusASTC] Anisotropic Filter: " + status(hasAnisotropic));
        System.out.println("[NexusASTC] Pixel Local Store : " + status(hasPixelLocalStore));
        System.out.println("[NexusASTC] Framebuffer Fetch : " + status(hasFramebufferFetch));
        System.out.println("[NexusASTC] ────────────────────────────────────────");

        if (!hasASTC_LDR) {
            System.err.println("[NexusASTC] AVISO: ASTC LDR não suportado — mod desativado");
        }
    }

    private static String status(boolean val) {
        return val ? "✅ SUPORTADO" : "❌ AUSENTE";
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public static boolean isASTCSupported()      { return hasASTC_LDR; }
    public static boolean isHDRSupported()       { return hasASTC_HDR; }
    public static boolean isDecodeModeSupported(){ return hasASTC_DecodeMode; }
    public static boolean isAnisotropicSupported(){ return hasAnisotropic; }
    public static boolean isPixelLocalStorage()  { return hasPixelLocalStore; }
    public static boolean isFramebufferFetch()   { return hasFramebufferFetch; }

    public static boolean isInitialized()        { return initialized; }
}
