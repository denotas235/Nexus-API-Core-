package com.maliopt.gpu;

import com.maliopt.MaliOptMod;
import com.nexuapicore.core.FeatureRegistry;

/**
 * ExtensionActivator — popula flags a partir do FeatureRegistry da Nexus API Core.
 * Não força nada. A fonte de verdade é o registry que vem do driver real.
 */
public class ExtensionActivator {

    // ── TIER 1 ──────────────────────────────────────────────────────
    public static boolean hasDiscardFramebuffer          = false;
    public static boolean hasMaliShaderBinary            = false;
    public static boolean hasMaliProgramBinary           = false;
    public static boolean hasGetProgramBinary            = false;
    public static boolean hasParallelShaderCompile       = false;

    // ── TIER 2 ──────────────────────────────────────────────────────
    public static boolean hasTextureStorage              = false;
    public static boolean hasBufferStorage               = false;
    public static boolean hasPackedDepthStencil          = false;
    public static boolean hasDepth24                     = false;
    public static boolean hasMultisampledRenderToTexture = false;

    // ── TIER 3 ──────────────────────────────────────────────────────
    public static boolean hasShaderPixelLocalStorage     = false;
    public static boolean hasFramebufferFetch            = false;
    public static boolean hasFramebufferFetchDepth       = false;

    // ── TIER 4 ──────────────────────────────────────────────────────
    public static boolean hasAstcLdr                     = false;
    public static boolean hasAstcHdr                     = false;

    private static boolean activated = false;

    /**
     * Popula as flags a partir do FeatureRegistry.
     * Chamado uma vez por MaliOptMod.applyMaliOpt().
     */
    public static void activateFromRegistry(FeatureRegistry registry) {
        if (activated) return;
        activated = true;

        MaliOptMod.LOGGER.info("[MaliOpt] ══════ Extensões via Nexus API Core ══════");

        // TIER 1
        hasDiscardFramebuffer    = reg(registry, "DISCARD_FRAMEBUFFER");
        hasMaliShaderBinary      = reg(registry, "MALI_SHADER_BINARY");
        hasMaliProgramBinary     = reg(registry, "MALI_PROGRAM_BINARY");
        hasGetProgramBinary      = reg(registry, "GET_PROGRAM_BINARY");
        hasParallelShaderCompile = reg(registry, "PARALLEL_SHADER_COMPILE");

        // TIER 2
        hasTextureStorage              = reg(registry, "TEXTURE_STORAGE");
        hasBufferStorage               = reg(registry, "BUFFER_STORAGE");
        hasPackedDepthStencil          = reg(registry, "PACKED_DEPTH_STENCIL");
        hasDepth24                     = reg(registry, "DEPTH24");
        hasMultisampledRenderToTexture = reg(registry, "MULTISAMPLED_RENDER_TO_TEXTURE");

        // TIER 3
        hasShaderPixelLocalStorage = reg(registry, "PIXEL_LOCAL_STORAGE");
        hasFramebufferFetch        = reg(registry, "FRAMEBUFFER_FETCH");
        hasFramebufferFetchDepth   = reg(registry, "FRAMEBUFFER_FETCH_DEPTH_STENCIL");

        // TIER 4
        hasAstcLdr = reg(registry, "ASTC_LDR");
        hasAstcHdr = reg(registry, "ASTC_HDR");

        logTier();
    }

    private static boolean reg(FeatureRegistry registry, String cap) {
        boolean v = registry.isAvailable(cap);
        MaliOptMod.LOGGER.info("[MaliOpt]   {} {}", v ? "✅" : "❌", cap);
        return v;
    }

    private static void logTier() {
        int tier = 0;
        if (hasDiscardFramebuffer && hasGetProgramBinary)           tier = 1;
        if (tier == 1 && hasTextureStorage)                         tier = 2;
        if (tier == 2 && hasShaderPixelLocalStorage && hasFramebufferFetch) tier = 3;

        MaliOptMod.LOGGER.info("[MaliOpt] ══════════════════════════════════════");
        MaliOptMod.LOGGER.info("[MaliOpt] Nível de optimização: TIER {}/3", tier);
        if (hasParallelShaderCompile)
            MaliOptMod.LOGGER.info("[MaliOpt] ⚡ Compilação paralela ACTIVA");
        if (hasShaderPixelLocalStorage)
            MaliOptMod.LOGGER.info("[MaliOpt] ⚡ Pixel Local Storage DISPONÍVEL");
        if (hasFramebufferFetch)
            MaliOptMod.LOGGER.info("[MaliOpt] ⚡ Framebuffer Fetch DISPONÍVEL");
        if (hasAstcLdr)
            MaliOptMod.LOGGER.info("[MaliOpt] ⚡ ASTC LDR DISPONÍVEL");
    }

    public static boolean isActivated() { return activated; }
}
