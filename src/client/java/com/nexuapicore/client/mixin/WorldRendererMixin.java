package com.nexuapicore.client.mixin;

import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.loader.api.FabricLoader;

/**
 * WorldRendererMixin — aplica o shadow map como post-process no TAIL.
 * Usa reflexao para evitar dependencia hard-coded em nexus-maliopt.
 *
 * O init() do ShadowPass e feito por NexusCoreGameRendererMixin no primeiro frame.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    private static final boolean HAS_MALIOPT =
        FabricLoader.getInstance().isModLoaded("nexus-maliopt");

    @Inject(
        method = "render",
        at = @At("TAIL"),
        require = 0
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
        } catch (Throwable ignored) {}
    }
}

