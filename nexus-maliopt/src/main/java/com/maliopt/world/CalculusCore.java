package com.maliopt.world;

import net.minecraft.util.math.Vec3d;
import java.util.LinkedList;

/**
 * CalculusCore — mede velocidade e aceleração para prever a próxima posição.
 * Usa uma janela deslizante de últimos N pontos (default 5) para calcular
 * derivadas de primeira (velocidade) e segunda ordem (aceleração).
 */
public class CalculusCore {
    private static final int WINDOW_SIZE = 5;
    private static final LinkedList<Vec3d> positionHistory = new LinkedList<>();
    private static Vec3d lastVelocity = Vec3d.ZERO;
    private static Vec3d predictedPosition = null;

    public static void feedPosition(Vec3d currentPos) {
        positionHistory.addLast(currentPos);
        if (positionHistory.size() > WINDOW_SIZE) {
            positionHistory.removeFirst();
        }
        updatePrediction();
    }

    private static void updatePrediction() {
        if (positionHistory.size() < 2) {
            lastVelocity = Vec3d.ZERO;
            predictedPosition = null;
            return;
        }
        Vec3d p0 = positionHistory.get(positionHistory.size() - 2);
        Vec3d p1 = positionHistory.getLast();
        // velocidade instantânea (delta t = 1 frame ~ 50ms, mas usamos 1 unidade de tempo)
        Vec3d velocity = p1.subtract(p0);
        // aceleração se tivermos pelo menos 3 pontos
        Vec3d acceleration = Vec3d.ZERO;
        if (positionHistory.size() >= 3) {
            Vec3d pPrev = positionHistory.get(positionHistory.size() - 3);
            Vec3d vPrev = p0.subtract(pPrev);
            acceleration = velocity.subtract(vPrev);
        }
        lastVelocity = velocity;
        // prediz a próxima posição: p1 + v + 0.5*a
        predictedPosition = p1.add(velocity).add(acceleration.multiply(0.5));
    }

    public static Vec3d getVelocity() {
        return lastVelocity;
    }

    public static Vec3d getPredictedPosition() {
        return predictedPosition;
    }

    public static double getSpeed() {
        return lastVelocity.length();
    }
}
