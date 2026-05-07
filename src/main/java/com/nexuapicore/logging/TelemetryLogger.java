package com.nexuapicore.logging;

import java.util.concurrent.atomic.AtomicInteger;

public class TelemetryLogger {
    private static final NexusLogger logger = NexusLogger.get("NexusTelemetry");
    private static final AtomicInteger frameCount = new AtomicInteger(0);
    private static long lastSecond = System.nanoTime();
    private static double currentFps = 0;
    private static double currentFrameTime = 0;
    
    public static void onFrameEnd(double frameTimeMs) {
        int frames = frameCount.incrementAndGet();
        long now = System.nanoTime();
        long elapsed = now - lastSecond;
        if (elapsed >= 1_000_000_000L) { // 1 segundo
            currentFps = frames / (elapsed / 1_000_000_000.0);
            currentFrameTime = frameTimeMs;
            logger.performanceSnapshot(currentFps, currentFrameTime);
            frameCount.set(0);
            lastSecond = now;
        }
    }
    
    public static double getCurrentFps() { return currentFps; }
    public static double getCurrentFrameTime() { return currentFrameTime; }
}
