package com.nexus.shadows;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.*;

public class NexusShadowsClient implements ClientModInitializer {
    private static boolean shadowInit = false;

    @Override
    public void onInitializeClient() {
        System.out.println("[Shadows] ═══ Module loading ═══");

        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[Shadows] nexus-api-core detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[Shadows] nexus-maliopt detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-render-hdr")) {
            System.out.println("[Shadows] nexus-render-hdr detected.");
        }

        // Aplica PCF depois das entidades e antes do tonemapping
        WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
            if (!shadowInit) {
                ShadowPipeline.init();
                shadowInit = true;
            }
            if (!ShadowPipeline.isReady()) return;
            int prog = ShadowPCFShader.getProgram();
            int vao  = ShadowPCFShader.getQuadVao();
            if (prog == 0 || vao == 0) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null || mc.getFramebuffer() == null) return;
            int fbo = mc.getFramebuffer().fbo;
            int w = mc.getWindow().getFramebufferWidth();
            int h = mc.getWindow().getFramebufferHeight();
            int shadowTex = ShadowPipeline.getShadowTexture();
            if (shadowTex == 0) return;

            int prevFbo  = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            GL11.glViewport(0, 0, w, h);
            GL20.glUseProgram(prog);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getFramebuffer().getColorAttachment());
            GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uScene"), 0);

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTex);
            GL20.glUniform1i(GL20.glGetUniformLocation(prog, "uShadowMap"), 1);

            GL20.glUniform2f(GL20.glGetUniformLocation(prog, "uScreenSize"), (float) w, (float) h);

            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
            GL20.glUseProgram(prevProg);
        });

        System.out.println("[Shadows] ═══ Module ready ═══");
    }
}
