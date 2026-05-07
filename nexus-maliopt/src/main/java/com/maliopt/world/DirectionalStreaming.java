package com.maliopt.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

/**
 * Decide quantos chunks carregar em cada direção (frente, trás, lados, cima, baixo)
 * com base no estado de movimento e na predição do MotionTracker.
 */
public class DirectionalStreaming {
    public static int front = 8, back = 2, left = 4, right = 4, up = 2, down = 4;

    public static void update() {
        MotionTracker.MovementState state = MotionTracker.getMovementState();
        double speed = MotionTracker.getSpeed();
        boolean changing = MotionTracker.isChangingDirection();

        switch (state) {
            case STILL:
                front = 4; back = 2; left = 3; right = 3; up = 1; down = 2;
                break;
            case WALKING:
                front = 6; back = 3; left = 4; right = 4; up = 2; down = 3;
                break;
            case RUNNING:
                front = 8; back = 3; left = 5; right = 5; up = 2; down = 4;
                break;
            case FLYING:
                front = 12; back = 4; left = 6; right = 6; up = 4; down = 4;
                break;
            case BOOSTING:
                front = 16; back = 4; left = 7; right = 7; up = 6; down = 6;
                break;
        }

        if (changing) {
            left = Math.max(left, front);
            right = Math.max(right, front);
        }

        double vert = MotionTracker.getVerticalSpeed();
        if (vert > 0.2) { // a subir
            up = Math.min(8, up + 2);
            down = Math.max(1, down - 1);
        } else if (vert < -0.2) { // a descer
            down = Math.min(8, down + 2);
            up = Math.max(1, up - 1);
        }
    }

    public static int getFrontRadius() { return front; }
    public static int getBackRadius()  { return back; }
}
