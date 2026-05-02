package com.nexuapicore.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class PerformanceGuard {
    private static final int VIEW_MIN = 2;
    private static final int VIEW_MAX = 32;
    private static int originalViewDistance = -1;
    private static ParticlesMode originalParticles = null;
    private static int lowFpsCounter = 0;
    private static int stableCounter = 0;
    private static final int LOW_FPS_THRESHOLD = 20;
    private static final int RECOVERY_FPS_THRESHOLD = 50;
    private static final int LOW_FPS_TICKS_TRIGGER = 3;
    private static final int RECOVERY_TICKS_TRIGGER = 5;
    private static boolean emergencyActive = false;

    public static void init() {
        if (originalViewDistance == -1) {
            MinecraftClient client = MinecraftClient.getInstance();
            originalViewDistance = client.options.getViewDistance().getValue();
            originalParticles = client.options.getParticles().getValue();
        }
    }

    public static void onFrame(int fps) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null) return;
        GameOptions opts = client.options;

        // Guarda original se ainda não guardado
        if (originalViewDistance == -1) {
            init();
        }

        if (fps < LOW_FPS_THRESHOLD) {
            lowFpsCounter++;
            stableCounter = 0;
            if (lowFpsCounter >= LOW_FPS_TICKS_TRIGGER && !emergencyActive) {
                emergencyActive = true;
                // Reduzir view distance e partículas
                int newView = Math.max(opts.getViewDistance().getValue() / 2, VIEW_MIN);
                opts.getViewDistance().setValue(newView);
                opts.getParticles().setValue(ParticlesMode.MINIMAL);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§cPerformanceGuard: reduzindo qualidade para manter FPS..."), true);
                }
            }
        } else if (fps >= RECOVERY_FPS_THRESHOLD) {
            stableCounter++;
            lowFpsCounter = 0;
            if (stableCounter >= RECOVERY_TICKS_TRIGGER && emergencyActive) {
                emergencyActive = false;
                // Restaurar original
                opts.getViewDistance().setValue(originalViewDistance);
                opts.getParticles().setValue(originalParticles);
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§aPerformanceGuard: qualidade restaurada."), true);
                }
            }
        } else {
            lowFpsCounter = 0;
            stableCounter = 0;
        }
    }

    public static void onTick() {
        // pode ser usado para ajustes menos agressivos
    }
}
