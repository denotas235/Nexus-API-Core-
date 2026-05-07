package com.maliopt.world;

import com.maliopt.MaliOptMod;
import com.maliopt.performance.PerformanceGuard;
import net.minecraft.client.option.GameOptions;
import java.util.function.Consumer;

public class NeuralLODController {
    private static int baseViewDistance = 8;
    private static int maxViewDistance  = 16;
    private static Consumer<Integer> viewSetter = null;
    private static GameOptions options = null;

    public static void init(GameOptions opts) {
        options = opts;
        if (options != null) {
            baseViewDistance = 8; // forçado para Elytra
            maxViewDistance = baseViewDistance;
        }
        MaliOptMod.LOGGER.info("[NeuralLOD] Inicializado com view distance base: 8{}", baseViewDistance);
    }

    public static void tick() {
        if (options == null) return;

        double speed = CalculusCore.getSpeed();
        int target;

        PerformanceGuard.StressLevel stress = PerformanceGuard.getStressLevel();
        switch (stress) {
            case LOW:
                // Com folga, pode esticar até ao máximo, mas com limite
                int boost = (int) Math.min(speed * 0.15, 4);
                target = Math.min(baseViewDistance + boost, maxViewDistance);
                break;
            case MEDIUM:
                target = baseViewDistance;
                break;
            case HIGH:
                target = Math.max(4, baseViewDistance - 2);
                break;
            case CRITICAL:
                target = 2; // mínimo absoluto
                break;
            default:
                target = baseViewDistance;
        }

        if (target != options.getViewDistance().getValue()) {
            options.getViewDistance().setValue(target);
            MaliOptMod.LOGGER.info("[NeuralLOD] ViewDistance ajustado para {} (stress: {}, speed: {})", target, stress, speed);
        }
    }
}
