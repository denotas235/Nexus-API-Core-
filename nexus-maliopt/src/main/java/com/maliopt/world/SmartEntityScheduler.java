package com.maliopt.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import java.util.*;

/**
 * Mantém o estado das entidades atrás do jogador sem as renderizar.
 * Entidades à frente são priorizadas; as de trás são "congeladas".
 */
public class SmartEntityScheduler {
    private static final Set<UUID> frozenEntities = new HashSet<>();
    private static Vec3d lastPlayerPos = Vec3d.ZERO;
    private static float lastPlayerYaw = 0f;

    public static boolean shouldRender(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return true;

        Vec3d playerPos = mc.player.getPos();
        Vec3d entityPos = entity.getPos();
        float playerYaw = mc.player.getYaw();

        // Vetor do jogador para a entidade
        Vec3d toEntity = entityPos.subtract(playerPos);
        double dist = toEntity.length();

        // Distância máxima de renderização
        if (dist > 64.0) {
            freeze(entity);
            return false;
        }

        // Ângulo entre o olhar do jogador e a direção da entidade
        Vec3d lookDir = Vec3d.fromPolar(0, playerYaw);
        double dot = toEntity.normalize().dotProduct(lookDir);

        // Se a entidade está atrás do jogador (dot < -0.3) e longe (> 16 blocos), congela
        if (dot < -0.3 && dist > 16.0) {
            freeze(entity);
            return false;
        }

        // Descongela se estiver perto ou à frente
        unfreeze(entity);
        return true;
    }

    private static void freeze(Entity entity) {
        frozenEntities.add(entity.getUuid());
        // Aqui poderíamos guardar o estado (posição, velocidade, etc.) para depois restaurar
    }

    private static void unfreeze(Entity entity) {
        frozenEntities.remove(entity.getUuid());
    }

    public static boolean isFrozen(Entity entity) {
        return frozenEntities.contains(entity.getUuid());
    }
}
