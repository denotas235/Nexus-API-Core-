package com.maliopt.performance;

import com.maliopt.MaliOptMod;

public class PerformanceGuard {
    public enum StressLevel { LOW, MEDIUM, HIGH, CRITICAL }

    private static final int TARGET_FPS = 60;
    // Thresholds mais permissivos (o Mali não precisa de ser tão exigente)
    private static final int GOOD_FPS = 55;   // acima disto = LOW
    private static final int OK_FPS   = 45;   // 45-55 = MEDIUM
    private static final int BAD_FPS  = 30;   // 30-45 = HIGH, abaixo = CRITICAL

    private static long lastCheckTime = System.nanoTime();
    private static int  frameCount    = 0;
    private static double currentFps  = 60.0;
    private static StressLevel stress = StressLevel.LOW;
    private static boolean initialized = false;

    public static void init() {
        initialized = true;
        MaliOptMod.LOGGER.info("[PerfGuard] Target: {} FPS (thresholds relaxados)", TARGET_FPS);
    }

    public static void onFrameEnd() {
        if (!initialized) return;
        frameCount++;
        long now = System.nanoTime();
        long elapsedNs = now - lastCheckTime;
        if (elapsedNs >= 1_000_000_000L) {
            currentFps = frameCount / (elapsedNs / 1_000_000_000.0);
            frameCount = 0;
            lastCheckTime = now;
            if (currentFps >= GOOD_FPS) stress = StressLevel.LOW;
            else if (currentFps >= OK_FPS) stress = StressLevel.MEDIUM;
            else if (currentFps >= BAD_FPS) stress = StressLevel.HIGH;
            else stress = StressLevel.CRITICAL;
            if (stress != StressLevel.LOW)
                MaliOptMod.LOGGER.warn("[PerfGuard] FPS: {:.1f} — Stress: {}", currentFps, stress);
        }
    }

    public static boolean isFpsHealthy() { return stress == StressLevel.LOW; }
    public static StressLevel getStressLevel() { return stress; }
    public static double getCurrentFps() { return currentFps; }

    // Métodos para passes de render
    public static boolean bloomEnabled()        { return true; }
    public static float bloomThreshold()        { return 0.3f; }
    public static float bloomRadius()           { return 1.5f; }
    public static float bloomIntensity()        { return 0.7f; }
    public static boolean lightingPassEnabled() { return true; }
    public static float warmth()                { return 0.3f; }
    public static float ambientOcclusion()      { return 0.6f; }
}
