package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class NexusTexturesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[NexusASTC] ── Nexus Textures Inicializando ─────────");

        // Inicializa deteção de extensões GL após contexto estar pronto
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ASTCDecodeMode.init();

            if (!ASTCDecodeMode.isASTCSupported()) {
                System.err.println("[NexusASTC] Hardware não suporta ASTC — mod inativo");
                return;
            }

            System.out.println("[NexusASTC] Hardware ASTC confirmado — mod ativo");
            System.out.println("[NexusASTC] Cache dir: " + ASTCCache.getCacheDir());
        });

        // Shutdown limpo
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            System.out.println("[NexusASTC] A encerrar recompressor...");
            BackgroundRecompressor.shutdown();
            ASTCUploadQueue.clear();
            System.out.println("[NexusASTC] Encerrado. Texturas THOROUGH: "
                + BackgroundRecompressor.getCompleted());
        });

        // Log de progresso a cada 200 frames
        WorldRenderEvents.END.register(context -> {
            int pending = BackgroundRecompressor.getPending();
            if (pending > 0) {
                // Log apenas ocasionalmente para não spam
                long frame = net.minecraft.client.MinecraftClient
                    .getInstance().getLastFrameDuration() > 0
                    ? System.currentTimeMillis() / 5000
                    : -1;
                if (frame % 1 == 0) {
                    System.out.println("[NexusASTC] Recompressão pendente: "
                        + pending + " texturas");
                }
            }
        });

        System.out.println("[NexusASTC] ─────────────────────────────────────────");
    }
}
