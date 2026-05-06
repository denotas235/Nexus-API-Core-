package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import java.io.*;

public final class ASTCEncoder {

    private static boolean nativeAvailable = false;
    private static boolean nativeInited    = false;

    private ASTCEncoder() {}

    public static void init() {
        try {
            System.loadLibrary("astc_bridge_64");
            nativeAvailable = true;
            MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so carregada do sistema");
        } catch (UnsatisfiedLinkError e) {
            try {
                String arch = System.getProperty("os.arch", "").contains("64") ? "arm64-v8a" : "armeabi-v7a";
                String libName = "libastc_bridge_64.so";
                String resourcePath = "/natives/" + arch + "/" + libName;
                InputStream in = ASTCEncoder.class.getResourceAsStream(resourcePath);
                if (in != null) {
                    File tmpLib = new File(System.getProperty("java.io.tmpdir"), libName);
                    try (FileOutputStream out = new FileOutputStream(tmpLib)) {
                        byte[] buf = new byte[8192]; int n;
                        while ((n = in.read(buf)) >= 0) out.write(buf, 0, n);
                    }
                    in.close();
                    System.load(tmpLib.getAbsolutePath());
                    nativeAvailable = true;
                    MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so extraída do JAR");
                } else {
                    MaliOptMod.LOGGER.warn("[ASTCEncoder] libastc_bridge_64.so não encontrada no classpath");
                }
            } catch (Exception ex) {
                MaliOptMod.LOGGER.warn("[ASTCEncoder] Falha ao extrair lib: {}", ex.getMessage());
            }
        }

        // Só chama initASTC() se a lib carregou
        if (nativeAvailable) {
            try {
                nativeInited = initASTC();
                if (!nativeInited) {
                    MaliOptMod.LOGGER.warn("[ASTCEncoder] initASTC() retornou false — encoder desactivado");
                    nativeAvailable = false;
                }
            } catch (UnsatisfiedLinkError ex) {
                MaliOptMod.LOGGER.warn("[ASTCEncoder] initASTC() não encontrado na lib: {}", ex.getMessage());
                nativeAvailable = false;
            }
        }
    }

    public static boolean isNativeAvailable() { return nativeAvailable && nativeInited; }

    public static byte[] compress(int width, int height, int blockX, int blockY, byte[] rgbaData) {
        if (!isNativeAvailable()) return null;
        try {
            return compressASTC(width, height, blockX, blockY, rgbaData);
        } catch (Exception e) {
            MaliOptMod.LOGGER.warn("[ASTCEncoder] compressASTC falhou: {}", e.getMessage());
            return null;
        }
    }

    public static native boolean initASTC();
    public static native byte[] compressASTC(int width, int height, int blockX, int blockY, byte[] rgbaData);
}
