package com.nexus.shadows;

import org.lwjgl.opengl.*;
import net.minecraft.client.MinecraftClient;

public class ShadowPipeline {
    private static int shadowFbo = 0;
    private static int shadowTex = 0;
    private static int shadowWidth  = 1024;
    private static int shadowHeight = 1024;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;

        // Cria FBO para o shadow map (depth only)
        shadowFbo = GL30.glGenFramebuffers();
        shadowTex = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT,
                shadowWidth, shadowHeight, 0,
                GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        // Clamp na borda para evitar artefactos
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 0x812F); // GL_CLAMP_TO_EDGE
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 0x812F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D, shadowTex, 0);
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
        if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("[Shadows] Shadow FBO incomplete: " + status);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        ShadowMapShader.compile();
        ShadowPCFShader.compile();
        System.out.println("[Shadows] Shadow pipeline initialized.");
    }

    public static void renderShadowPass(MinecraftClient mc) {
        if (shadowFbo == 0 || mc.world == null) return;
        int prog = ShadowMapShader.getProgram();
        if (prog == 0) return;

        int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevViewport[] = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
        GL11.glViewport(0, 0, shadowWidth, shadowHeight);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(prog);

        // Matriz de luz simples (sol direcional)
        float[] lightView = new float[16];
        // Identidade por enquanto – a câmara do sol será adicionada depois
        for (int i = 0; i < 16; i++) lightView[i] = 0f;
        lightView[0] = 1f; lightView[5] = 1f; lightView[10] = 1f; lightView[15] = 1f;
        GL20.glUniformMatrix4fv(
            GL20.glGetUniformLocation(prog, "uLightView"), false, lightView);

        // Desenha a cena (o Minecraft fará as chamadas de draw; aqui apenas ativamos o shader)
        // A injeção real será feita no mixin do WorldRenderer.

        GL20.glUseProgram(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
    }

    public static int getShadowTexture() {
        return shadowTex;
    }

    public static boolean isReady() {
        return initialized && shadowFbo != 0 && ShadowPCFShader.getProgram() != 0;
    }
}
