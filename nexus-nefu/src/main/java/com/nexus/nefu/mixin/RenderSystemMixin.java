package com.nexus.nefu.mixin;

import com.nexus.nefu.BatchManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * RenderSystemMixin — interceta chamadas de draw para fazer batching de primitivos.
 *
 * Fix: o ramo else nao cancelava ci, causando double-draw (o metodo original
 * executava apos a chamada manual a glDrawArrays). Agora cancela sempre e
 * faz o draw manualmente.
 *
 * Fix: lastMode e agora reposto em BatchManager.reset() apos flush.
 *
 * NOTA: require=0 evita crash se o metodo drawArrays nao existir nesta versao
 * do Minecraft (a API RenderSystem muda entre versoes menores).
 */
@Mixin(value = RenderSystem.class, remap = false)
public class RenderSystemMixin {

    @Inject(
        method = "drawArrays",
        at = @At("HEAD"),
        cancellable = true,
        remap = false,
        require = 0
    )
    private static void onDrawArrays(int mode, int first, int count, CallbackInfo ci) {
        // Cancela SEMPRE — fazemos o draw manualmente nos dois ramos
        ci.cancel();

        if (BatchManager.shouldBatch(mode, count)) {
            // Acumula no lote actual
            BatchManager.addToBatch(mode, first, count);
        } else {
            // Envia o lote anterior (modo diferente)
            BatchManager.flush();
            // Desenha a nova chamada directamente
            GL11.glDrawArrays(mode, first, count);
            // Inicia novo estado de batching
            BatchManager.resetMode(mode);
        }
    }
}

