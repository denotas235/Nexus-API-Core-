package com.nexus.render.hdr;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.*;

public class NexusRenderHdrClient implements ClientModInitializer {
    private static boolean hdrInitialized = false;

    @Override
    public void onInitializeClient() {
        System.out.println("[NexusRenderHDR] ═══ Module loading ═══");

        // Deteção de mods complementares
        if (FabricLoader.getInstance().isModLoaded("nexus-api-core")) {
            System.out.println("[NexusRenderHDR] nexus-api-core detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-maliopt")) {
            System.out.println("[NexusRenderHDR] nexus-maliopt detected.");
        }
        if (FabricLoader.getInstance().isModLoaded("nexus-shadows")) {
            System.out.println("[NexusRenderHDR] nexus-shadows detected.");
        }

        // Aplica tonemapping APÓS o mundo e ANTES da GUI
        WorldRenderEvents.END.register(ctx -> {
            if (!hdrInitialized) {
                HdrPipeline.init();
                hdrInitialized = true;
            }
            if (!HdrPipeline.isReady()) return;
            int program = TonemappingShader.getProgram();
            int vao = TonemappingShader.getQuadVao();
            if (program == 0 || vao == 0) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null || mc.getFramebuffer() == null) return;
            int fbo = mc.getFramebuffer().fbo;
            int w = mc.getWindow().getFramebufferWidth();
            int h = mc.getWindow().getFramebufferHeight();

            int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
            GL11.glViewport(0, 0, w, h);
            GL20.glUseProgram(program);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getFramebuffer().getColorAttachment());
            GL20.glUniform1i(GL20.glGetUniformLocation(program, "uScene"), 0);
            GL30.glBindVertexArray(vao);
            GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL30.glBindVertexArray(0);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
            GL20.glUseProgram(prevProg);
        });

        System.out.println("[NexusRenderHDR] ═══ Module ready ═══");
    }
}
