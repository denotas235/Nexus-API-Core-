package com.maliopt.world;

import com.maliopt.MaliOptMod;
import net.minecraft.client.MinecraftClient;

/**
 * Classifica automaticamente o jogador num perfil (Builder, Miner, Explorer, Elytra Pilot, Engineer)
 * com base nas ações recentes. Sem menus — 100% automático.
 */
public class RuntimeBehaviorSwitch {
    public enum Profile { BUILDER, MINER, EXPLORER, ELYTRA_PILOT, ENGINEER }

    private static final int WINDOW_SECONDS = 30;
    private static int blocksBroken = 0, blocksPlaced = 0, redstoneInteractions = 0;
    private static long lastReset = System.currentTimeMillis();
    private static Profile currentProfile = Profile.EXPLORER;

    public static void onBlockBroken() {
        blocksBroken++;
        decayCounters();
    }

    public static void onBlockPlaced() {
        blocksPlaced++;
        decayCounters();
    }

    public static void onRedstoneInteraction() {
        redstoneInteractions++;
        decayCounters();
    }

    public static void tick() {
        decayCounters();
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        // Elytra tem prioridade máxima
        if (mc.player.isFallFlying()) {
            currentProfile = Profile.ELYTRA_PILOT;
            return;
        }

        double speed = MotionTracker.getSpeed();
        boolean isMoving = speed > 4.0;

        // Determinar perfil dominante
        if (redstoneInteractions > blocksBroken && redstoneInteractions > blocksPlaced) {
            currentProfile = Profile.ENGINEER;
        } else if (blocksBroken > blocksPlaced && blocksBroken > 5) {
            currentProfile = Profile.MINER;
        } else if (blocksPlaced > blocksBroken && blocksPlaced > 5) {
            currentProfile = Profile.BUILDER;
        } else if (isMoving && speed > 7.0) {
            currentProfile = Profile.EXPLORER;
        } else if (isMoving) {
            currentProfile = Profile.EXPLORER;
        } else {
            // mantém o perfil anterior se nenhum dominante
        }
    }

    private static void decayCounters() {
        long now = System.currentTimeMillis();
        if (now - lastReset > WINDOW_SECONDS * 1000L) {
            blocksBroken /= 2;
            blocksPlaced /= 2;
            redstoneInteractions /= 2;
            lastReset = now;
        }
    }

    public static Profile getCurrentProfile() { return currentProfile; }

    public static String getProfileName() {
        return switch(currentProfile) {
            case BUILDER -> "Builder";
            case MINER -> "Miner";
            case EXPLORER -> "Explorer";
            case ELYTRA_PILOT -> "Elytra Pilot";
            case ENGINEER -> "Engineer";
        };
    }
}
