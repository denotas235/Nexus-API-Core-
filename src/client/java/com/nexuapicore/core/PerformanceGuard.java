package com.nexuapicore.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;

/**
 * PerformanceGuard — Protecção automática de performance para todos os mods Nexus.
 *
 * Actua nas camadas de cima do jogo (view distance, partículas).
 * NUNCA toca no TDBR, simulation distance, ou VSync.
 *
 * Limiares:
 *   ≥ 45 FPS  → Confortável  — nada muda
 *   30–44 FPS → Alerta       — partículas → Mínimo
 *   20–29 FPS → Crítico      — view distance 4→3
 *   < 20 FPS por 3s → Emergência — view distance 3→2
 *   ≥ 50 FPS por 5s → Recuperação — restaura tudo ao padrão
 *
 * Anti-spike:
 *   tick > 1000ms → aviso no log
 *   3 ticks consecutivos > 500ms → view distance baixa 1 temporariamente
 */
public class PerformanceGuard {

    // ── Constantes ────────────────────────────────────────────────────────
    private static final int FPS_COMFORTABLE    = 45;
    private static final int FPS_ALERT          = 30;
    private static final int FPS_CRITICAL       = 20;
    private static final int FPS_RECOVERY       = 50;

    private static final int VIEW_DEFAULT       = 4;
    private static final int VIEW_MIN           = 2;

    private static final long MS_EMERGENCY      = 3_000; // 3s abaixo de 20 FPS
    private static final long MS_RECOVERY       = 5_000; // 5s acima de 50 FPS
    private static final long MS_SPIKE_WARN     = 1_000; // tick > 1000ms → aviso
    private static final long MS_SPIKE_ACT      = 500;   // tick > 500ms → conta spike
    private static final int  SPIKE_LIMIT       = 3;     // 3 spikes consecutivos → actua

    // ── Estado interno ────────────────────────────────────────────────────
    private static boolean initialized          = false;
    private static boolean particlesReduced     = false;
    private static boolean viewReducedCritical  = false;
    private static boolean viewReducedEmergency = false;
    private static boolean viewReducedSpike     = false;

    private static long lowFpsStartMs           = -1;
    private static long highFpsStartMs          = -1;
    private static int  consecutiveSpikes       = 0;
    private static long lastTickMs              = -1;

    // ── Init ──────────────────────────────────────────────────────────────
    public static void init() {
        if (initialized) return;
        initialized = true;
        System.out.println("[PerformanceGuard] Inicializado — protecção activa para todos os mods Nexus");
    }

    // ── Chamado a cada frame (WorldRenderEvents.END) ──────────────────────
    public static void onFrame() {
        if (!initialized) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.currentScreen != null) return; // não actua em menus

        int fps = mc.getCurrentFps();
        GameOptions opts = mc.options;
        if (opts == null) return;

        long now = System.currentTimeMillis();

        // ── Recuperação ───────────────────────────────────────────────────
        if (fps >= FPS_RECOVERY) {
            if (highFpsStartMs < 0) highFpsStartMs = now;
            if (now - highFpsStartMs >= MS_RECOVERY) {
                restore(opts);
                highFpsStartMs = -1;
            }
            lowFpsStartMs = -1;
            return;
        } else {
            highFpsStartMs = -1;
        }

        // ── Confortável ───────────────────────────────────────────────────
        if (fps >= FPS_COMFORTABLE) {
            lowFpsStartMs = -1;
            return;
        }

        // ── Alerta: partículas → Mínimo ───────────────────────────────────
        if (fps < FPS_COMFORTABLE && fps >= FPS_ALERT) {
            if (!particlesReduced) {
                setParticles(opts, 2); // 0=All 1=Decreased 2=Minimal
                particlesReduced = true;
                System.out.println("[PerformanceGuard] Alerta (" + fps + " FPS) — partículas → Mínimo");
            }
            lowFpsStartMs = -1;
            return;
        }

        // ── Crítico: view distance 4→3 ────────────────────────────────────
        if (fps < FPS_ALERT && fps >= FPS_CRITICAL) {
            if (!viewReducedCritical) {
                int current = getViewDistance(opts);
                if (current > VIEW_MIN + 1) {
                    setViewDistance(opts, Math.max(current - 1, VIEW_MIN + 1));
                    viewReducedCritical = true;
                    System.out.println("[PerformanceGuard] Crítico (" + fps + " FPS) — view distance → " + getViewDistance(opts));
                }
            }
            lowFpsStartMs = -1;
            return;
        }

        // ── Emergência: < 20 FPS por 3s → view distance → 2 ─────────────
        if (fps < FPS_CRITICAL) {
            if (lowFpsStartMs < 0) lowFpsStartMs = now;
            if (now - lowFpsStartMs >= MS_EMERGENCY) {
                if (!viewReducedEmergency) {
                    int current = getViewDistance(opts);
                    if (current > VIEW_MIN) {
                        setViewDistance(opts, VIEW_MIN);
                        viewReducedEmergency = true;
                        System.out.println("[PerformanceGuard] Emergência (" + fps + " FPS por 3s) — view distance → " + VIEW_MIN);
                    }
                }
            }
        }
    }

    // ── Chamado a cada tick do servidor integrado ─────────────────────────
    public static void onTick() {
        if (!initialized) return;

        long now = System.currentTimeMillis();
        if (lastTickMs < 0) {
            lastTickMs = now;
            return;
        }

        long tickDuration = now - lastTickMs;
        lastTickMs = now;

        // Aviso de spike grave
        if (tickDuration > MS_SPIKE_WARN) {
            System.out.println("[PerformanceGuard] SPIKE GRAVE: tick demorou " + tickDuration + "ms");
            consecutiveSpikes = 0; // reset — spike grave é isolado
            return;
        }

        // Conta spikes moderados consecutivos
        if (tickDuration > MS_SPIKE_ACT) {
            consecutiveSpikes++;
            if (consecutiveSpikes >= SPIKE_LIMIT) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc != null && mc.options != null) {
                    int current = getViewDistance(mc.options);
                    if (current > VIEW_MIN) {
                        setViewDistance(mc.options, current - 1);
                        viewReducedSpike = true;
                        System.out.println("[PerformanceGuard] " + SPIKE_LIMIT + " spikes consecutivos (" + tickDuration + "ms) — view distance → " + getViewDistance(mc.options));
                    }
                }
                consecutiveSpikes = 0;
            }
        } else {
            consecutiveSpikes = 0;
        }
    }

    // ── Restaura tudo ao padrão ───────────────────────────────────────────
    private static void restore(GameOptions opts) {
        boolean restored = false;

        if (particlesReduced) {
            setParticles(opts, 0); // All
            particlesReduced = false;
            restored = true;
        }
        if (viewReducedCritical || viewReducedEmergency || viewReducedSpike) {
            setViewDistance(opts, VIEW_DEFAULT);
            viewReducedCritical  = false;
            viewReducedEmergency = false;
            viewReducedSpike     = false;
            restored = true;
        }

        if (restored) {
            System.out.println("[PerformanceGuard] Recuperação — tudo restaurado ao padrão");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private static void setViewDistance(GameOptions opts, int value) {
        try {
            opts.viewDistance().setValue(Math.max(value, VIEW_MIN));
        } catch (Throwable t) {
            System.out.println("[PerformanceGuard] setViewDistance falhou: " + t.getMessage());
        }
    }

    private static int getViewDistance(GameOptions opts) {
        try {
            return (int) opts.viewDistance().getValue();
        } catch (Throwable t) {
            return VIEW_DEFAULT;
        }
    }

    private static void setParticles(GameOptions opts, int level) {
        try {
            // 0=All 1=Decreased 2=Minimal
            opts.particles().setValue(
                net.minecraft.client.option.ParticlesMode.byId(level)
            );
        } catch (Throwable t) {
            System.out.println("[PerformanceGuard] setParticles falhou: " + t.getMessage());
        }
    }
}
