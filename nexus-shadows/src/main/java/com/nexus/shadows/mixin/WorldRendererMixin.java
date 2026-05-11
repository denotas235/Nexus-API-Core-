package com.nexus.shadows.mixin;

import com.nexus.shadows.ShadowPipeline;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(
        method = "render",
        at = @At("HEAD"),
        require = 0  // nao falha se a assinatura do metodo mudar entre versoes
    )
    private void onRenderHead(
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f positionMatrix,
            Matrix4f projectionMatrix,
            CallbackInfo ci) {
        if (!ShadowPipeline.isReady()) return;
        float td = tickCounter.getTickDelta(true);
        ShadowPipeline.renderShadowPass(td);
    }
}