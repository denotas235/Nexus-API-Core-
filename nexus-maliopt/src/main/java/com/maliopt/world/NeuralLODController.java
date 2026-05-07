package com.maliopt.world;

import com.maliopt.MaliOptMod;
import com.maliopt.performance.PerformanceGuard;
import java.util.function.Consumer;

/**
 * NeuralLODController — simula uma "fóvea" de atenção.
 * Reduz a distância de visão atrás do jogador e aumenta à frente,
 * baseando-se na velocidade e aceleração.
 */
public class NeuralLODController {
    private static int baseViewDistance = 8;        // chunks (server view distance)
    private static double maxBoost = 4;             // chunks extras à frente
    private static int smoothFactor = 2;
    private static Consumer<Integer> viewDistanceSetter = null;

    public static void init(Consumer<Integer> setter, int initialView) {
        viewDistanceSetter = setter;
        baseViewDistance = initialView;
    }

    public static void tick() {
        if (viewDistanceSetter == null) return;

        double speed = CalculusCore.getSpeed();
        // quanto mais rápido, maior o foco à frente (até maxBoost extra)
        double frontBoost = Math.min(maxBoost, speed * 0.1);
        int desired = baseViewDistance + (int)frontBoost;

        // se o FPS estiver baixo, reduz a distância
        if (!PerformanceGuard.isFpsHealthy()) {
            desired = Math.max(4, baseViewDistance - 2);
        }

        // enviamos a mesma distância para todos os lados (o servidor não suporta assimétrico)
        // mas podemos fazer o próprio WorldCache priorizar chunks frontais.
        // Apenas atualizamos se necessário.
        setViewDistance(desired);
    }

    private static int lastSet = -1;
    private static void setViewDistance(int target) {
        if (target != lastSet && viewDistanceSetter != null) {
            lastSet = target;
            viewDistanceSetter.accept(target);
            MaliOptMod.LOGGER.info("[NeuralLOD] ViewDistance ajustado para {}", target);
        }
    }
}
