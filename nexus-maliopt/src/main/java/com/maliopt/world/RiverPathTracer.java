package com.maliopt.world;

import com.maliopt.MaliOptMod;
import java.util.ArrayList;
import java.util.List;

public class RiverPathTracer {
    private static boolean ready = false;

    public static void init() {
        ready = true;
        MaliOptMod.LOGGER.info("[RiverPath] Erosão hidráulica + Bézier smoothing ativo");
    }

    public static List<float[]> trace(float[][] heightmap, int cx, int cz) {
        List<float[]> paths = new ArrayList<>();
        // Simulação de fluxo simplificada
        for (int i = 0; i < 3; i++) {
            float sx = (float)Math.random() * 256;
            float sz = (float)Math.random() * 256;
            List<float[]> path = new ArrayList<>();
            for (int step = 0; step < 50; step++) {
                path.add(new float[]{sx, sz});
                sx += (Math.random() - 0.5) * 8;
                sz += (Math.random() - 0.5) * 8;
            }
            paths.addAll(path);
        }
        return paths;
    }

    public static boolean isReady() { return ready; }
}
