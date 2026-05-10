package com.nexus.render.hdr.mixin;

import com.nexus.render.hdr.HdrPipeline;
import com.nexus.render.hdr.TonemappingShader;
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
    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void onRenderTail(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        if (!HdrPipeline.isReady()) return;
        int program = TonemappingShader.getProgram();
        int vao = TonemappingShader.getQuadVao();
        if (program == 0 || vao == 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getFramebuffer() == null) return;
        int fbo = mc.getFramebuffer().fbo;
        int w = mc.getWindow().getFramebufferWidth();
        int h = mc.getWindow().getFramebufferHeight();

        // Aplicar o tonemapping como um fullscreen quad
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
    }
}
