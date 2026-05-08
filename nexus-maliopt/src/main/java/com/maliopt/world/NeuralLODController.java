package com.maliopt.world;

import com.maliopt.MaliOptMod;
import com.maliopt.performance.PerformanceGuard;
import net.minecraft.client.option.GameOptions;

public class NeuralLODController {
    private static int baseViewDistance = 8;
    private static int maxViewDistance  = 16;
    private static GameOptions options = null;
    private static int lastSet = -1;

    public static void init(GameOptions opts) {
        options = opts;
        if (options != null) {
            baseViewDistance = 8;
            maxViewDistance = 16;
        }
        MaliOptMod.LOGGER.info("[NeuralLOD] Inicializado com view distance base: {}", baseViewDistance);
    }

    public static void tick() {
        if (options == null) return;

        double speed = MotionTracker.getSpeed();
        MotionTracker.MovementState mov = MotionTracker.getMovementState();
        PerformanceGuard.StressLevel stress = PerformanceGuard.getStressLevel();
        int target;

        switch (stress) {
            case LOW:
                if (mov == MotionTracker.MovementState.BOOSTING) {
                    target = maxViewDistance;
                } else if (mov == MotionTracker.MovementState.FLYING) {
                    target = baseViewDistance + 4;
                } else if (mov == MotionTracker.MovementState.RUNNING) {
                    target = baseViewDistance + 2;
                } else {
                    target = baseViewDistance;
                }
                break;
            case MEDIUM:
                target = mov == MotionTracker.MovementState.BOOSTING ? baseViewDistance + 2 : baseViewDistance;
                break;
            case HIGH:
                target = Math.max(4, baseViewDistance - 2);
                break;
            case CRITICAL:
                target = 4; // mínimo 4 chunks
                break;
            default:
                target = baseViewDistance;
        }

        if (target != lastSet && options != null) {
            lastSet = target;
            options.getViewDistance().setValue(target);
            MaliOptMod.LOGGER.info("[NeuralLOD] ViewDistance ajustado para {} (state: {}, stress: {}, speed: {})",
                target, mov, stress, String.format("%.1f", speed));
        }
    }
}
