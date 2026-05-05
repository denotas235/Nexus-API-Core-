package com.maliopt.shader;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.GPUDetector;
import com.nexuapicore.core.FeatureRegistry;

public final class ShaderCapabilities {

    public static boolean PLS              = false;
    public static boolean FB_FETCH         = false;
    public static boolean FB_FETCH_DEPTH   = false;
    public static boolean ASTC             = false;
    public static boolean ASTC_HDR         = false;
    public static boolean MULTISAMPLED_RT  = false;
    public static boolean TBDR             = false;
    public static boolean BIFROST          = false;
    public static boolean VALHALL          = false;
    public static boolean MEDIUMP_FAST     = false;
    public static boolean FP16_ARITHMETIC  = false;

    private static boolean initialised     = false;

    private ShaderCapabilities() {}

    /**
     * Inicializa as capacidades a partir do FeatureRegistry da Nexus API Core.
     * Esta é a única fonte de verdade para extensões.
     */
    public static void init(FeatureRegistry registry) {
        if (initialised) return;

        // ── Extensões de framebuffer ─────────────────────────────
        PLS            = registry.isAvailable("PIXEL_LOCAL_STORAGE");
        FB_FETCH       = registry.isAvailable("FRAMEBUFFER_FETCH");
        FB_FETCH_DEPTH = registry.isAvailable("FRAMEBUFFER_FETCH_DEPTH_STENCIL");

        // ── Compressão de texturas ───────────────────────────────
        ASTC           = registry.isAvailable("ASTC_LDR");
        ASTC_HDR       = registry.isAvailable("ASTC_HDR");

        // ── Render targets ───────────────────────────────────────
        MULTISAMPLED_RT = registry.isAvailable("MULTISAMPLED_RENDER_TO_TEXTURE");

        // ── Arquitectura GPU ─────────────────────────────────────
        String model = GPUDetector.getGPUModel();
        if (model == null) model = "";
        TBDR    = model.contains("Mali-G");
        BIFROST = model.matches(".*Mali-G(31|51|52|71|72|76|77).*");
        VALHALL = model.matches(".*Mali-G(57|68|78|310|510|610|710|615|715).*");
        MEDIUMP_FAST = TBDR;

        initialised = true;
        logCapabilities();
    }

    public static boolean canUseTBDRPath() {
        return TBDR && (PLS || FB_FETCH);
    }

    public static boolean isInitialised() { return initialised; }

    private static void logCapabilities() {
        MaliOptMod.LOGGER.info("[MaliOpt] ═══ ShaderCapabilities (via Nexus) ═══");
        MaliOptMod.LOGGER.info("[MaliOpt]  PLS:              {}", PLS);
        MaliOptMod.LOGGER.info("[MaliOpt]  FB_FETCH:         {}", FB_FETCH);
        MaliOptMod.LOGGER.info("[MaliOpt]  FB_FETCH_DEPTH:   {}", FB_FETCH_DEPTH);
        MaliOptMod.LOGGER.info("[MaliOpt]  ASTC:             {}", ASTC);
        MaliOptMod.LOGGER.info("[MaliOpt]  ASTC_HDR:         {}", ASTC_HDR);
        MaliOptMod.LOGGER.info("[MaliOpt]  MULTISAMPLED_RT:  {}", MULTISAMPLED_RT);
        MaliOptMod.LOGGER.info("[MaliOpt]  TBDR:             {}", TBDR);
        MaliOptMod.LOGGER.info("[MaliOpt]  BIFROST:          {}", BIFROST);
        MaliOptMod.LOGGER.info("[MaliOpt]  MEDIUMP_FAST:     {}", MEDIUMP_FAST);
        MaliOptMod.LOGGER.info("[MaliOpt] ══════════════════════════════════════");
    }
}
