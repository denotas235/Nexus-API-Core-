package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import java.io.*;

public final class ASTCEncoder {

    private static boolean nativeAvailable = false;
    private static boolean nativeInited    = false;

    private ASTCEncoder() {}

    public static void init() {
        // Tentar carregar a biblioteca nativa
        try {
            System.loadLibrary("astc_bridge_64");
            nativeAvailable = true;
            MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so carregada do sistema");
        } catch (UnsatisfiedLinkError e) {
            MaliOptMod.LOGGER.warn("[ASTCEncoder] Falha ao carregar libastc_bridge_64.so: {}", e.getMessage());
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
                    MaliOptMod.LOGGER.info("[ASTCEncoder] libastc_bridge_64.so extraída do JAR em {}", tmpLib.getAbsolutePath());
                } else {
                    MaliOptMod.LOGGER.warn("[ASTCEncoder] libastc_bridge_64.so não encontrada no classpath (resource: {})", resourcePath);
                }
            } catch (Exception ex) {
                MaliOptMod.LOGGER.error("[ASTCEncoder] Erro ao extrair lib: {}", ex.getMessage());
            }
        }

        if (nativeAvailable) {
            try {
                nativeInited = initASTC();
                if (!nativeInited) {
                    MaliOptMod.LOGGER.warn("[ASTCEncoder] initASTC() retornou false");
                    nativeAvailable = false;
                } else {
                    MaliOptMod.LOGGER.info("[ASTCEncoder] initASTC() OK. Teste de compressão...");
                    byte[] testRgba = new byte[4 * 4 * 4]; // 4x4 pixels RGBA
                    byte[] compressed = compressASTC(4, 4, 4, 4, testRgba);
                    if (compressed != null && compressed.length > 0) {
                        MaliOptMod.LOGGER.info("[ASTCEncoder] Teste de compressão OK ({} bytes)", compressed.length);
                    } else {
                        MaliOptMod.LOGGER.warn("[ASTCEncoder] Teste de compressão falhou (nulo ou vazio)");
                        nativeAvailable = false;
                    }
                }
            } catch (UnsatisfiedLinkError ex) {
                MaliOptMod.LOGGER.error("[ASTCEncoder] initASTC() não encontrado: {}", ex.getMessage());
                nativeAvailable = false;
            }
        } else {
            MaliOptMod.LOGGER.warn("[ASTCEncoder] Biblioteca nativa indisponível. Texturas não serão comprimidas.");
        }
    }

    public static boolean isNativeAvailable() { return nativeAvailable && nativeInited; }

    public static byte[] compress(int width, int height, int blockX, int blockY, byte[] rgbaData) {
        if (!isNativeAvailable()) return null;
        try {
            return compressASTC(width, height, blockX, blockY, rgbaData);
        } catch (Exception e) {
            MaliOptMod.LOGGER.warn("[ASTCEncoder] compress falhou: {}", e.getMessage());
            return null;
        }
    }

    public static native boolean initASTC();
    public static native byte[] compressASTC(int width, int height, int blockX, int blockY, byte[] rgbaData);
}
