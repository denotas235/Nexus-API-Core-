package com.maliopt.astc;

import com.maliopt.MaliOptMod;

/**
 * ASTCEncoder — compressão PNG → ASTC via libastc_bridge_64.so
 *
 * Usa a .so que já está em natives/arm64-v8a/libastc_bridge_64.so
 */
public final class ASTCEncoder {

    private static boolean nativeAvailable = false;

    private ASTCEncoder() {}

    public static void init() {
        try {
            System.loadLibrary("astc_bridge_64");
            nativeAvailable = true;
            MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so carregada");
        } catch (UnsatisfiedLinkError e) {
            nativeAvailable = false;
            MaliOptMod.LOGGER.warn("[ASTCEncoder] libastc_bridge_64.so não disponível");
        }
    }

    public static boolean isNativeAvailable() {
        return nativeAvailable;
    }

    // Métodos nativos (implementados na libastc_bridge_64.so)
    public static native boolean initASTC();
    public static native byte[] compressASTC(
        int width, int height,
        int blockX, int blockY,
        byte[] rgbaData
    );
}
