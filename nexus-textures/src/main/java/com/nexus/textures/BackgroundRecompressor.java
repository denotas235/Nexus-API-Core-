package com.nexus.textures;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundRecompressor {
    private static final ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Nexus-ASTC-Recomp");
        t.setDaemon(true);
        return t;
    });

    public static void schedule(String path, byte[] rgba, int w, int h, ASTCTextureCategory cat) {
        pool.submit(() -> {
            byte[] highQ = ASTCEncoder.compress(rgba, w, h, cat, true); // thorough
            if (highQ != null && highQ.length >= 16) {
                ASTCCache.put(path, highQ);
                System.out.println("[NexusASTC] Recompressed (thorough): " + path);
            }
        });
    }
}
