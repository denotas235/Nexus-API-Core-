package com.nexus.render.hdr;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

/**
 * Pipeline HDR para Mali-G52 MC2 (ARM64).
 *
 * Nao chamar init() fora do contexto GL (nao chamar em onInitializeClient).
 * Usar GameRendererMixin que invoca initGL() no primeiro frame de render.
 */
public class HdrPipeline {

    // GL_FRAMEBUFFER_SRGB  (OpenGL 3.0 / GL_EXT_sRGB_write_control)
    private static final int GL_FRAMEBUFFER_SRGB = 0x8DB9;
    // GL_TEXTURE_MAX_ANISOTROPY_EXT
    private static final int GL_TEXTURE_MAX_ANISOTROPY     = 0x84FE;
    // GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
    private static final int GL_MAX_TEXTURE_MAX_ANISOTROPY = 0x84FF;

    private static boolean sRGB        = false;
    private static boolean anisotropic = false;
    private static float   maxAniso    = 1.0f;
    private static boolean acesReady   = false;
    private static boolean ready       = false;

    /**
     * Chamado pelo GameRendererMixin no primeiro frame — GL esta disponivel.
     */
    public static void initGL() {
        if (ready) return;

        try {
            GLCapabilities caps = GL.getCapabilities();

            // ── sRGB framebuffer ─────────────────────────────────────────────
            // Disponivel em OpenGL 3.0 ou via GL_EXT_sRGB_write_control (Mali)
            if (caps.OpenGL30) {
                try {
                    GL11.glEnable(GL_FRAMEBUFFER_SRGB);
                    sRGB = true;
                    NexusRenderHdrClient.LOGGER.info("[NexusHDR] sRGB framebuffer ativado.");
                } catch (Exception e) {
                    NexusRenderHdrClient.LOGGER.warn("[NexusHDR] sRGB enable falhou: {}", e.getMessage());
                }
            } else {
                NexusRenderHdrClient.LOGGER.warn("[NexusHDR] sRGB nao suportado pelo driver.");
            }

            // ── Anisotropic filtering ────────────────────────────────────────
            if (caps.GL_EXT_texture_filter_anisotropic) {
                maxAniso = GL11.glGetFloat(GL_MAX_TEXTURE_MAX_ANISOTROPY);
                maxAniso = Math.min(maxAniso, 16.0f);
                anisotropic = true;
                NexusRenderHdrClient.LOGGER.info("[NexusHDR] Anisotropic filtering ativo ({}x).", (int) maxAniso);
            } else {
                NexusRenderHdrClient.LOGGER.warn("[NexusHDR] GL_EXT_texture_filter_anisotropic nao suportado.");
            }

            // ── ACES Tonemapping shader ──────────────────────────────────────
            TonemappingShader.compile();
            acesReady = TonemappingShader.getProgram() != 0;
            NexusRenderHdrClient.LOGGER.info("[NexusHDR] ACES shader: {}.",
                    acesReady ? "compilado" : "nao disponivel");

        } catch (Exception e) {
            NexusRenderHdrClient.LOGGER.error("[NexusHDR] Pipeline GL falhou: {}", e.getMessage());
        } finally {
            ready = true;
            NexusRenderHdrClient.LOGGER.info(
                "[NexusHDR] Pipeline pronto — sRGB:{} AF:{}({}x) ACES:{}",
                sRGB, anisotropic, (int) maxAniso, acesReady);
        }
    }

    public static boolean isReady()        { return ready; }
    public static boolean hasSRGB()        { return sRGB; }
    public static boolean hasAnisotropic() { return anisotropic; }
    public static float   getMaxAnisotropy(){ return maxAniso; }
    public static boolean hasACES()        { return acesReady; }

    /**
     * Aplica filtragem anisotropica a textura atualmente ligada (GL_TEXTURE_2D).
     * Chamar apos glBindTexture.
     */
    public static void applyAnisotropic() {
        if (!anisotropic) return;
        try {
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL_TEXTURE_MAX_ANISOTROPY, maxAniso);
        } catch (Exception ignored) {}
    }
}