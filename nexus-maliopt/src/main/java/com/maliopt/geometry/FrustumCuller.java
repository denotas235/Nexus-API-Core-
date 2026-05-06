package com.maliopt.geometry;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class FrustumCuller {
    private static boolean enabled = false;

    public static void init() {
        enabled = true;
        MaliOptMod.LOGGER.info("[SFCRS] FrustumCuller activo (GL_EXT_primitive_bounding_box)");
    }

    public static boolean isEnabled() { return enabled; }
}
