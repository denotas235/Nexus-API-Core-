package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import java.io.*;

public final class ASTCEncoder {

    private static boolean nativeAvailable = false;

    private ASTCEncoder() {}

    public static void init() {
        try {
            // Tenta carregar do sistema primeiro (mod nativo instalado)
            System.loadLibrary("astc_bridge_64");
            nativeAvailable = true;
            MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so carregada do sistema");
        } catch (UnsatisfiedLinkError e) {
            // Fallback: extrair do JAR
            try {
                String arch = System.getProperty("os.arch", "").contains("64") ? "arm64-v8a" : "armeabi-v7a";
                String libName = "libastc_bridge_64.so";
                String resourcePath = "/natives/" + arch + "/" + libName;
                InputStream in = ASTCEncoder.class.getResourceAsStream(resourcePath);
                if (in != null) {
                    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
                    File tmpLib = new File(tmpDir, libName);
                    try (FileOutputStream out = new FileOutputStream(tmpLib)) {
                        byte[] buf = new byte[8192];
                        int n;
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
    }

    public static boolean isNativeAvailable() {
        return nativeAvailable;
    }

    public static native boolean initASTC();
    public static native byte[] compressASTC(
        int width, int height,
        int blockX, int blockY,
        byte[] rgbaData
    );
}
