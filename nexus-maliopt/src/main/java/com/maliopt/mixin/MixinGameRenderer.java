package com.maliopt.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "render", at = @At("HEAD"), require = 1)
    private void onRenderStart(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        // Hook de início de frame – reservado para futuras métricas
    }

    @Inject(method = "render", at = @At("TAIL"), require = 1)
    private void onRenderEnd(RenderTickCounter tickCounter, boolean bl, CallbackInfo ci) {
        // Hook de fim de frame – reservado para limpeza
    }
}
