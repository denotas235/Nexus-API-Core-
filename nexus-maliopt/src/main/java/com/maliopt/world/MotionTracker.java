package com.maliopt.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class MotionTracker {
    private static Vec3d lastPos = null;
    private static Vec3d velocity = Vec3d.ZERO;
    private static Vec3d predictedPosition = null;
    private static float lastYaw = 0f;
    private static float yawDelta = 0f;
    private static MovementState state = MovementState.STILL;

    public enum MovementState { STILL, WALKING, RUNNING, FLYING, BOOSTING }

    public static void feedPosition(Vec3d currentPos) {
        lastPos = currentPos;
    }

    public static Vec3d getVelocity() { return velocity; }
    public static double getSpeed() { return velocity.length(); }
    public static Vec3d getPredictedPosition() { return predictedPosition; }
    public static MovementState getMovementState() { return state; }
    public static float getYaw() { return lastYaw; }
    public static float getYawDelta() { return yawDelta; }
    public static boolean isChangingDirection() { return yawDelta > 30f; }
    public static double getVerticalSpeed() { return velocity.y; }
}
