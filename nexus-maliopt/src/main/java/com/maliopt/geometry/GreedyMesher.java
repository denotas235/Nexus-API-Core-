package com.maliopt.geometry;

import com.maliopt.MaliOptMod;

public class GreedyMesher {
    private static boolean enabled = false;

    public static void init() {
        enabled = true;
        MaliOptMod.LOGGER.info("[SFTGS] GreedyMesher activo");
    }

    public static boolean isEnabled() { return enabled; }
}
