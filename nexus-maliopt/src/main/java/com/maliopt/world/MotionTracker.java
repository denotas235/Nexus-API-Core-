package com.maliopt.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MotionTracker {
    public enum MovementState {
        STILL,      // parado
        WALKING,    // a andar
        RUNNING,    // a correr (sprint)
        FLYING,     // Elytra ativa, sem foguete
        BOOSTING    // Elytra com foguete a acelerar
    }

    private static Vec3d lastPos = null;
    private static Vec3d velocity = Vec3d.ZERO;
    private static Vec3d acceleration = Vec3d.ZERO;
    private static Vec3d predictedPosition = null;
    private static float lastYaw = 0f;
    private static float yawDelta = 0f;
    private static MovementState state = MovementState.STILL;
    private static long lastBoostTime = 0;
    private static double avgSpeed = 0;
    private static int sampleCount = 0;

    public static void feedPosition(Vec3d currentPos) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        ClientPlayerEntity player = mc.player;

        // Velocidade
        if (lastPos != null) {
            velocity = currentPos.subtract(lastPos);
            // Aceleração (diferença de velocidade)
            if (sampleCount > 0) {
                Vec3d prevVel = lastPos.subtract(lastPos.add(velocity.multiply(-1))); // simplificado
            }
        }
        lastPos = currentPos;

        // Yaw e mudança de direção
        float yaw = player.getYaw();
        yawDelta = Math.abs(yaw - lastYaw);
        lastYaw = yaw;

        // Estado de movimento
        boolean isFlying = player.isFallFlying();
        double speed = velocity.length();

        if (isFlying) {
            if (player.isUsingItem()) { // foguete em uso
                state = MovementState.BOOSTING;
                lastBoostTime = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - lastBoostTime < 1500) {
                state = MovementState.BOOSTING; // ainda em boost
            } else {
                state = MovementState.FLYING;
            }
        } else if (speed < 0.1) {
            state = MovementState.STILL;
        } else if (player.isSprinting()) {
            state = MovementState.RUNNING;
        } else {
            state = MovementState.WALKING;
        }

        // Velocidade média (para previsão)
        avgSpeed = (avgSpeed * sampleCount + speed) / (sampleCount + 1);
        sampleCount = Math.min(sampleCount + 1, 60); // 3 segundos a 20 tps

        // Predição de posição (1 chunk = 16 blocos)
        if (speed > 0.01) {
            Vec3d dir = velocity.normalize();
            double lookAhead = speed * 2.0; // 2 segundos à frente
            predictedPosition = currentPos.add(dir.multiply(lookAhead));
        } else {
            predictedPosition = currentPos;
        }
    }

    // Getters para o NeuralLODController
    public static Vec3d getVelocity() { return velocity; }
    public static double getSpeed() { return velocity.length(); }
    public static Vec3d getPredictedPosition() { return predictedPosition; }
    public static MovementState getMovementState() { return state; }
    public static float getYaw() { return lastYaw; }
    public static float getYawDelta() { return yawDelta; }
    public static double getAverageSpeed() { return avgSpeed; }
    public static double getVerticalSpeed() { return velocity.y; }
    public static boolean isChangingDirection() { return yawDelta > 30f; }
}
