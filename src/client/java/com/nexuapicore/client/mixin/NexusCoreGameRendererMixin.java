package com.nexuapicore.client.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.loader.api.FabricLoader;

/**
 * NexusCoreGameRendererMixin — adia o init() do ShadowPass (nexus-maliopt)
 * para o primeiro frame de render, quando o contexto OpenGL esta disponivel.
 *
 * Nao chamar ShadowPass.init() em onInitializeClient() ou CLIENT_STARTED
 * pois o contexto GL pode ainda nao estar pronto.
 */
@Mixin(GameRenderer.class)
public class NexusCoreGameRendererMixin {

    private static boolean nexusShadowInitDone = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void nexus_onFirstFrame(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (nexusShadowInitDone) return;
        nexusShadowInitDone = true;
        if (!FabricLoader.getInstance().isModLoaded("nexus-maliopt")) return;
        try {
            Class.forName("com.maliopt.pipeline.ShadowPass")
                 .getMethod("init")
                 .invoke(null);
        } catch (Exception e) {
            System.err.println("[Nexus] ShadowPass.init() via reflection falhou: " + e.getMessage());
        }
    }
}

