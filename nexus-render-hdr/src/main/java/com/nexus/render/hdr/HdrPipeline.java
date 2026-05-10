package com.nexus.render.hdr;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLES30;
import java.nio.ByteBuffer;

public class HdrPipeline {
    private static boolean sRGBWriteControl = false;
    private static boolean anisotropicFilter = false;
    private static float maxAnisotropy = 1.0f;
    private static int tonemapProgram = 0;
    private static int quadVao = 0;
    private static int hdrFbo = 0;
    private static int hdrTex = 0;
    private static int lastW = 0, lastH = 0;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        String extensions = GL11.glGetString(GL11.GL_EXTENSIONS);
        System.out.println("[HDR] Extensions: " + (extensions != null ? "OK" : "NULL"));

        // sRGB write control (GL_EXT_sRGB_write_control)
        sRGBWriteControl = extensions != null && extensions.contains("GL_EXT_sRGB_write_control");
        if (sRGBWriteControl) {
            GL11.glEnable(0x8BF2); // GL_FRAMEBUFFER_SRGB_EXT
            System.out.println("[HDR] sRGB framebuffer ENABLED");
        } else {
            System.out.println("[HDR] sRGB framebuffer NOT available");
        }

        // Anisotropic filtering (GL_EXT_texture_filter_anisotropic)
        anisotropicFilter = extensions != null && extensions.contains("GL_EXT_texture_filter_anisotropic");
        if (anisotropicFilter) {
            float[] max = new float[1];
            try {
                GL11.glGetFloatv(0x84FF, max); // GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT
                maxAnisotropy = max[0];
            } catch (Exception e) {
                maxAnisotropy = 16.0f;
            }
            System.out.println("[HDR] Anisotropic filter available (max " + maxAnisotropy + "x)");
        } else {
            System.out.println("[HDR] Anisotropic filter NOT available");
        }

        // Compilar shader de tonemapping
        tonemapProgram = TonemappingShader.compile();
        if (tonemapProgram != 0) {
            quadVao = GL30.glGenVertexArrays();
            System.out.println("[HDR] Tonemapping shader compiled (program " + tonemapProgram + ")");
        } else {
            System.out.println("[HDR] Tonemapping shader NOT compiled – pipeline works without post‑process");
        }

        System.out.println("[HDR] Pipeline initialized.");
    }

    public static void applyAnisotropic(int textureId) {
        if (!anisotropicFilter || textureId == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, 0x84FE, maxAnisotropy); // GL_TEXTURE_MAX_ANISOTROPY_EXT
    }

    public static boolean hasSRGB() { return sRGBWriteControl; }
    public static boolean hasAnisotropic() { return anisotropicFilter; }
    public static boolean isReady() { return initialized; }

    // ── Tonemapping pass (aplicado no fim do frame) ──────────────────
    public static void applyTonemapping(int sceneFbo, int width, int height) {
        if (tonemapProgram == 0 || quadVao == 0) return;

        // Criar FBO HDR se necessário
        if (width != lastW || height != lastH) {
            if (hdrFbo != 0) GL30.glDeleteFramebuffers(hdrFbo);
            if (hdrTex != 0) GL11.glDeleteTextures(hdrTex);
            hdrTex = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, hdrTex);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0,
                    GL11.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            hdrFbo = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, hdrFbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                    GL11.GL_TEXTURE_2D, hdrTex, 0);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
            lastW = width; lastH = height;
        }

        int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        // Copiar cena para FBO HDR
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, sceneFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, hdrFbo);
        GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height,
                GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        // Aplicar tonemapping
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, sceneFbo);
        GL11.glViewport(0, 0, width, height);
        GL20.glUseProgram(tonemapProgram);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, hdrTex);
        GL20.glUniform1i(GL20.glGetUniformLocation(tonemapProgram, "uScene"), 0);
        GL30.glBindVertexArray(quadVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL20.glUseProgram(prevProg);
    }

    public static void cleanup() {
        if (tonemapProgram != 0) { GL20.glDeleteProgram(tonemapProgram); tonemapProgram = 0; }
        if (quadVao != 0) { GL30.glDeleteVertexArrays(quadVao); quadVao = 0; }
        if (hdrFbo != 0) { GL30.glDeleteFramebuffers(hdrFbo); hdrFbo = 0; }
        if (hdrTex != 0) { GL11.glDeleteTextures(hdrTex); hdrTex = 0; }
        initialized = false;
        System.out.println("[HDR] Pipeline cleaned up.");
    }
}
