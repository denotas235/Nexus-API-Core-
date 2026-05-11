package com.nexus.shadows;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexusShadowsClient implements ClientModInitializer {
    public static final String MOD_ID = "nexus-shadows";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Shadows] ═══ Module loading ═══");

        if (FabricLoader.getInstance().isModLoaded("nexus-api-core"))   LOGGER.info("[Shadows] nexus-api-core detetado.");
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt"))    LOGGER.info("[Shadows] nexus-maliopt detetado — TBDR reduz custo do shadow pass.");
        if (FabricLoader.getInstance().isModLoaded("nexus-render-hdr")) LOGGER.info("[Shadows] nexus-render-hdr detetado — sombras aplicadas antes do tonemapping.");
        if (FabricLoader.getInstance().isModLoaded("nexus-textures"))   LOGGER.info("[Shadows] nexus-textures detetado — ASTC liberta VRAM para shadow map.");

        // Shadow pass: renderizado ANTES das entidades, do ponto de vista da luz
        WorldRenderEvents.BEFORE_ENTITIES.register(ctx -> {
            if (!ShadowPipeline.isReady()) return;
            float td = ctx.tickCounter().getTickDelta(true);
            ShadowPipeline.renderShadowPass(td);
        });

        // PCF pass: pós-processamento aplicado APÓS as entidades
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if (!ShadowPipeline.isReady()) return;
            int prog    = ShadowPCFShader.getProgram();
            int vao     = ShadowPCFShader.getQuadVao();
            int shadowT = ShadowPipeline.getShadowTexture();
            if (prog == 0 || vao == 0 || shadowT == 0) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null || mc.getFramebuffer() == null) return;

            int fbo = mc.getFramebuffer().fbo;
            int w   = mc.getWindow().getFramebufferWidth();
            int h   = mc.getWindow().getFramebufferHeight();

            int prevFbo  = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            GL11.glViewport(0, 0, w, h);
            GL20.glUseProgram(prog);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getFramebuffer().getColorAttachment());
            GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uScene"), 0);

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowT);
            GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uShadowMap"), 1);

            GL20.glUniform2f(GL20.glGetUniformLocation(prog, "uScreenSize"), (float) w, (float) h);

            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
            GL20.glUseProgram(prevProg);
        });

        LOGGER.info("[Shadows] ═══ Module registado — GL inicia no primeiro frame ═══");
    }
}