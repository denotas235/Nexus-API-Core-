package com.maliopt.performance;

import com.maliopt.MaliOptMod;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class PerformanceGuard {
    private static final int TARGET_FPS = 30;      // 30 ou 60, conforme config
    private static long lastFrameTime = 0;
    private static boolean healthy = true;
    private static int frameCount = 0;
    private static long lastCheck = 0;

    public static void init() {
        MaliOptMod.LOGGER.info("[PerfGuard] Target FPS: {}", TARGET_FPS);
    }

    public static void onFrameEnd() {
        long now = System.nanoTime();
        frameCount++;
        if (lastCheck == 0) lastCheck = now;
        long elapsedMs = (now - lastCheck) / 1_000_000;
        if (elapsedMs >= 1000) { // a cada 1 segundo
            double actualFps = frameCount / (elapsedMs / 1000.0);
            healthy = actualFps >= (TARGET_FPS * 0.75);
            if (!healthy) {
                MaliOptMod.LOGGER.warn("[PerfGuard] FPS baixo: {:.1f} (target {})", actualFps, TARGET_FPS);
            }
            frameCount = 0;
            lastCheck = now;
        }
    }

    public static boolean isFpsHealthy() {
        return healthy;
    }
}
