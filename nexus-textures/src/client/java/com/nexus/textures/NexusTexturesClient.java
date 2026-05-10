package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class NexusTexturesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        System.out.println("[NexusASTC] ── Nexus Textures Inicializando ─────────");

        // GL só está disponível após CLIENT_STARTED
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            try {
                ASTCDecodeMode.init();

                if (!ASTCDecodeMode.isASTCSupported()) {
                    System.err.println("[NexusASTC] ASTC não suportado — mod inativo");
                    return;
                }

                if (!ASTCEncoder.isAvailable()) {
                    System.err.println("[NexusASTC] astcenc não disponível — runtime compression desativada");
                }

                System.out.println("[NexusASTC] ✅ Mod ativo");
                System.out.println("[NexusASTC] Cache: " + ASTCCache.getCacheDir());
                System.out.println("[NexusASTC] astcenc: " + ASTCEncoder.isAvailable());
                System.out.println("[NexusASTC] HDR: " + ASTCDecodeMode.isHDRSupported());
                System.out.println("[NexusASTC] Decode Mode FP16: " + ASTCDecodeMode.isDecodeModeSupported());

            } catch (Exception e) {
                System.err.println("[NexusASTC] Erro na inicialização: " + e.getMessage());
            }
        });

        // Shutdown limpo
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                System.out.println("[NexusASTC] A encerrar...");
                BackgroundRecompressor.shutdown();
                ASTCUploadQueue.clear();
                System.out.println("[NexusASTC] Concluído. THOROUGH: "
                    + BackgroundRecompressor.getCompleted());
            } catch (Exception e) {
                System.err.println("[NexusASTC] Erro no shutdown: " + e.getMessage());
            }
        });

        System.out.println("[NexusASTC] ─────────────────────────────────────────");
    }
}
