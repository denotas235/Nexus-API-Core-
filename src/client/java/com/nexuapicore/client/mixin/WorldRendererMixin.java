package com.nexuapicore.client.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.loader.api.FabricLoader;

/**
 * WorldRendererMixin — aplica o shadow map (PCF) como post-process no TAIL.
 *
 * Usa reflexao para evitar dependencia hard-coded em nexus-maliopt.
 * Apenas activo se nexus-maliopt estiver instalado.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private static final boolean HAS_MALIOPT =
        FabricLoader.getInstance().isModLoaded("nexus-maliopt");

    /** Diferir init GL de ShadowPass para o primeiro frame de render. */
    @Mixin(GameRenderer.class)
    abstract static class GameRendererHook {
        private static boolean shadowInitDone = false;

        @Inject(method = "render", at = @At("HEAD"))
        private void onFirstFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
            if (!shadowInitDone && HAS_MALIOPT) {
                shadowInitDone = true;
                try {
                    Class.forName("com.maliopt.pipeline.ShadowPass")
                         .getMethod("init").invoke(null);
                } catch (Exception ignored) {}
            }
        }
    }

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void nexus_applyPostProcessShadows(
        RenderTickCounter tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        GameRenderer gameRenderer,
        LightmapTextureManager lightmapTextureManager,
        Matrix4f positionMatrix,
        Matrix4f projectionMatrix,
        CallbackInfo ci
    ) {
        if (!HAS_MALIOPT) return;
        try {
            Class<?> sp = Class.forName("com.maliopt.pipeline.ShadowPass");
            boolean isReady = (boolean) sp.getMethod("isReady").invoke(null);
            if (isReady) {
                sp.getMethod("applyToScreen").invoke(null);
            }
        } catch (Throwable t) {
            // Nao crashar o jogo
        }
    }
}

