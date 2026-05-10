package com.nexus.textures;

import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class BackgroundRecompressor {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "NexusASTC-Recompressor");
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY); // nunca interfere com o render
        return t;
    });

    private static final AtomicInteger pending   = new AtomicInteger(0);
    private static final AtomicInteger completed = new AtomicInteger(0);

    // Submete uma textura para recompressão THOROUGH em background
    public static void submit(
        byte[] pngBytes,
        int width,
        int height,
        ASTCTextureCategory category
    ) {
        // Já tem versão THOROUGH em cache — nada a fazer
        if (ASTCCache.hasThorough(pngBytes, category)) return;

        pending.incrementAndGet();

        EXECUTOR.submit(() -> {
            try {
                // Comprime com qualidade máxima
                byte[] astcThorough = ASTCEncoder.compress(
                    pngBytes, width, height, category, true
                );

                if (astcThorough != null) {
                    // Guarda versão THOROUGH no cache
                    ASTCCache.save(pngBytes, category, true, astcThorough);

                    // Remove versão FASTEST — já não é necessária
                    ASTCCache.evictFastest(pngBytes, category);

                    completed.incrementAndGet();
                    System.out.println("[NexusASTC] Recompressão THOROUGH concluída ["
                        + completed.get() + "/" + (completed.get() + pending.get()) + "] "
                        + category.name()
                        + " " + category.blockX + "x" + category.blockY);
                }

            } catch (Exception e) {
                System.err.println("[NexusASTC] Erro na recompressão: " + e.getMessage());
            } finally {
                pending.decrementAndGet();
            }
        });
    }

    public static int getPending()   { return pending.get(); }
    public static int getCompleted() { return completed.get(); }

    public static boolean isDone()   { return pending.get() == 0; }

    // Chamado no shutdown do jogo
    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }
    }
}
