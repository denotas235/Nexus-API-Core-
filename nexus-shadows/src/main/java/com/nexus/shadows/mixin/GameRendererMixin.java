package com.nexus.shadows.mixin;

import com.nexus.shadows.ShadowPipeline;
import com.nexus.shadows.ShadowPCFShader;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private static boolean shadowInit = false;

    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void onRenderHead(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        if (!shadowInit) {
            ShadowPipeline.init();
            shadowInit = true;
        }

        // Renderiza o shadow map antes da cena principal
        ShadowPipeline.renderShadowPass(mc);
    }

    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void onRenderTail(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;

        if (!ShadowPipeline.isReady()) return;
        int prog = ShadowPCFShader.getProgram();
        int vao  = ShadowPCFShader.getQuadVao();
        if (prog == 0 || vao == 0) return;

        int fbo = mc.getFramebuffer().fbo;
        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();

        int shadowTex = ShadowPipeline.getShadowTexture();
        if (shadowTex == 0) return;

        int prevFbo  = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        // Aplica PCF shadow como fullscreen quad
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
    }
}
