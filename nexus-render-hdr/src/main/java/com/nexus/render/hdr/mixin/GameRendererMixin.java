package com.nexus.render.hdr.mixin;

import com.nexus.render.hdr.HdrPipeline;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("TAIL"), require = 0)
    private void onRenderTail(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        if (!HdrPipeline.isReady()) return;
        // Tonemapping será adicionado aqui na próxima iteração
    }
}
