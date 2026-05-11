package com.nexus.textures;

import java.util.concurrent.atomic.AtomicInteger;

public class ASTCLoadingState {
    private static volatile int total = 0;
    private static final AtomicInteger loaded = new AtomicInteger(0);
    private static volatile boolean done = false;
    private static volatile long loadTimeMs = 0;
    private static volatile long showUntil = 0;
    private static final AtomicInteger runtimeUploads = new AtomicInteger(0);

    public static void begin(int totalCount) {
        total = totalCount;
        loaded.set(0);
        done = false;
        showUntil = 0;
    }

    public static void increment() {
        loaded.incrementAndGet();
    }

    public static void finish(long ms) {
        loadTimeMs = ms;
        done = true;
        showUntil = System.currentTimeMillis() + 6000;
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
    public static boolean shouldShowHud() { return !done || System.currentTimeMillis() < showUntil; }

    public static float getProgress() {
        if (total == 0) return 1.0f;
        return Math.min(1.0f, (float) loaded.get() / total);
    }
}