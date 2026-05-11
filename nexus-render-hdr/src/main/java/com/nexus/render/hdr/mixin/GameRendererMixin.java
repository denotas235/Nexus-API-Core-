package com.nexus.render.hdr.mixin;

import com.nexus.render.hdr.HdrPipeline;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adia a inicialização GL do pipeline HDR para o primeiro frame de render,
 * quando o contexto OpenGL já está garantidamente disponível.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    private static boolean nexusHdrInitDone = false;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (!nexusHdrInitDone) {
            nexusHdrInitDone = true;
            HdrPipeline.initGL();
        }
    }
}