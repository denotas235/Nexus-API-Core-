package com.nexus.streaming.mixin;

import com.nexus.streaming.StreamingPipeline;
import com.nexus.streaming.UploadQueue;
import com.nexus.streaming.ChunkBufferManager;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Interceta o agendamento de rebuild de chunks para enfileirar uploads incrementais.
 *
 * Alvo: net.minecraft.client.render.chunk.ChunkBuilder (Yarn 1.21.1)
 * require=0 — nao falha se a classe/metodo mudar entre versoes menores.
 */
@Mixin(targets = "net.minecraft.client.render.chunk.ChunkBuilder", require = 0)
public class ChunkBuilderMixin {

    @Inject(method = "scheduleRebuild(IIIbZ)V", at = @At("HEAD"), require = 0)
    private void onScheduleRebuild(int x, int y, int z, boolean important, boolean important2, CallbackInfo ci) {
        if (!StreamingPipeline.isInitialized()) return;
        long key = ChunkBufferManager.keyOf(x, y, z);
        // Sinalizar que este chunk precisa de upload incremental.
        // O dado real sera fornecido pelo pipeline de render (fase v1.1.0).
        // Por agora, pre-aloca o buffer persistente para este chunk.
        StreamingPipeline.getBufferManager().getOrCreateBuffer(key);
    }

    @Inject(method = "scheduleRebuild(Lnet/minecraft/client/render/chunk/ChunkRendererRegion;Z)V",
            at = @At("HEAD"), require = 0)
    private void onScheduleRebuildRegion(Object region, boolean important, CallbackInfo ci) {
        // Variante alternativa da assinatura em versoes diferentes do Yarn
        if (!StreamingPipeline.isInitialized()) return;
    }
}