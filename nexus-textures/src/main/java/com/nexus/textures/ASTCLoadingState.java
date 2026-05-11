package com.nexus.textures;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ASTCLoadingState — estado do carregamento de texturas ASTC.
 *
 * Fix: a barra de carregamento nao aparecia porque showUntil era calculado
 * em relacao ao momento em que onInitializeClient() terminava, que pode ser
 * vários segundos antes do primeiro frame ser renderizado (enquanto o Minecraft
 * carrega o mundo). O timestamp e agora definido na primeira chamada a
 * shouldShowHud() apos o carregamento estar concluido.
 */
public class ASTCLoadingState {
    private static volatile int total = 0;
    private static final AtomicInteger loaded = new AtomicInteger(0);
    private static volatile boolean done = false;
    private static volatile long loadTimeMs = 0;
    private static volatile long showUntil = Long.MAX_VALUE; // mostra ate ser explicitamente fechado
    private static volatile boolean firstHudCheck = false;
    private static final AtomicInteger runtimeUploads = new AtomicInteger(0);

    public static void begin(int totalCount) {
        total = totalCount;
        loaded.set(0);
        done = false;
        showUntil = Long.MAX_VALUE;
        firstHudCheck = false;
    }

    public static void increment() {
        loaded.incrementAndGet();
    }

    public static void finish(long ms) {
        loadTimeMs = ms;
        done = true;
        // showUntil sera definido no primeiro frame de render (quando o jogador realmente ve o HUD)
    }

    public static void trackRuntimeUpload() {
        runtimeUploads.incrementAndGet();
    }

    public static void resetRuntimeUploads() {
        runtimeUploads.set(0);
    }

    public static int getLoaded()         { return loaded.get(); }
    public static int getTotal()          { return total; }
    public static boolean isDone()        { return done; }
    public static long getLoadTimeMs()    { return loadTimeMs; }
    public static int getRuntimeUploads() { return runtimeUploads.get(); }

    /**
     * A barra deve aparecer durante o carregamento, e manter-se visivel
     * durante 6 segundos apos o primeiro frame em que e consultada com done=true.
     */
    public static boolean shouldShowHud() {
        if (!done) return total > 0; // mostra enquanto carrega (se houver texturas)
        // Primeiro frame apos conclusao: iniciar o temporizador de 6s
        if (!firstHudCheck) {
            firstHudCheck = true;
            showUntil = System.currentTimeMillis() + 6000;
        }
        return System.currentTimeMillis() < showUntil;
    }

    public static float getProgress() {
        if (total == 0) return 1.0f;
        return Math.min(1.0f, (float) loaded.get() / total);
    }
}

