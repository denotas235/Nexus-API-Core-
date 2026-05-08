package com.maliopt.pipeline;

import com.maliopt.MaliOptMod;
import com.maliopt.config.MaliOptConfig;
import com.maliopt.gpu.ExtensionActivator;
import com.maliopt.gpu.GPUDetector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengles.GLES30;

public class TileBasedOptimizer {

    private static boolean initialized = false;

    private static final int[] DEPTH_STENCIL_ATTACHMENTS = {
        GL30.GL_DEPTH_ATTACHMENT,
        GL30.GL_STENCIL_ATTACHMENT
    };

    private static final int[] ALL_ATTACHMENTS = {
        GL30.GL_COLOR_ATTACHMENT0,
        GL30.GL_DEPTH_ATTACHMENT,
        GL30.GL_STENCIL_ATTACHMENT
    };

    public static void init() {
        if (initialized || !GPUDetector.isMaliGPU()) return;
        if (!MaliOptConfig.enableTileOptimizer) return;
        initialized = true;

        MaliOptMod.LOGGER.info("[MaliOpt] TileBasedOptimizer iniciado");
        logCapabilities();
    }

    private static void logCapabilities() {
        MaliOptMod.LOGGER.info("[MaliOpt] TBDR capabilities:");

        if (ExtensionActivator.hasDiscardFramebuffer)
            MaliOptMod.LOGGER.info("[MaliOpt]   ✅ Depth/Stencil discard — bandwidth salvo por frame");

        if (ExtensionActivator.hasShaderPixelLocalStorage)
            MaliOptMod.LOGGER.info("[MaliOpt]   ✅ Pixel Local Storage — pronto para Fase 3");
        else
            MaliOptMod.LOGGER.info("[MaliOpt]   ❌ Pixel Local Storage — driver não expõe (GL4ES)");

        if (ExtensionActivator.hasFramebufferFetch)
            MaliOptMod.LOGGER.info("[MaliOpt]   ✅ Framebuffer Fetch — post-process grátis (Fase 4)");

        if (ExtensionActivator.hasFramebufferFetchDepth)
            MaliOptMod.LOGGER.info("[MaliOpt]   ✅ Framebuffer Fetch Depth — SSAO possível (Fase 4)");
    }

    public static void onFrameEnd() {
        if (!initialized) return;
        if (!ExtensionActivator.hasDiscardFramebuffer) return;

        try {
            GLES30.glInvalidateFramebuffer(GL30.GL_FRAMEBUFFER, DEPTH_STENCIL_ATTACHMENTS);
        } catch (Exception ignored) {}
    }

    public static void onAuxPassEnd() {
        if (!initialized) return;
        if (!ExtensionActivator.hasDiscardFramebuffer) return;

        try {
            GLES30.glInvalidateFramebuffer(GL30.GL_FRAMEBUFFER, ALL_ATTACHMENTS);
        } catch (Exception ignored) {}
    }

    public static boolean isPLSAvailable() {
        return ExtensionActivator.hasShaderPixelLocalStorage;
    }

    public static boolean isFramebufferFetchAvailable() {
        return ExtensionActivator.hasFramebufferFetch;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
