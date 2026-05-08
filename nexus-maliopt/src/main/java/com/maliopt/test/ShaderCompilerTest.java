package com.maliopt.test;

import com.maliopt.MaliOptMod;
import java.io.File;

public class ShaderCompilerTest {
    private static boolean tested = false;
    private static String result = "NÃO TESTADO";

    public static void runTest() {
        if (tested) return;
        tested = true;

        MaliOptMod.LOGGER.info("[ShaderTest] ═══ Testando libCNamaSDK.so ═══");

        String[] possiblePaths = {
            "/vendor/lib64/libCNamaSDK.so",
            "/system/lib64/libCNamaSDK.so",
            "/vendor/lib/libCNamaSDK.so",
            "/system/lib/libCNamaSDK.so"
        };

        String foundPath = null;
        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists()) {
                foundPath = path;
                MaliOptMod.LOGGER.info("[ShaderTest] Biblioteca encontrada em: {}", path);
                break;
            }
        }

        if (foundPath == null) {
            result = "BIBLIOTECA NÃO ENCONTRADA";
            MaliOptMod.LOGGER.warn("[ShaderTest] ❌ libCNamaSDK.so não encontrada no sistema");
            return;
        }

        try {
            System.load(foundPath);
            result = "CARREGADA COM SUCESSO";
            MaliOptMod.LOGGER.info("[ShaderTest] ✅ Biblioteca carregada com sucesso!");
        } catch (UnsatisfiedLinkError e) {
            result = "FALHA AO CARREGAR: " + e.getMessage();
            MaliOptMod.LOGGER.error("[ShaderTest] ❌ Falha ao carregar: {}", e.getMessage());
        }

        MaliOptMod.LOGGER.info("[ShaderTest] ═══ Teste concluído: {} ═══", result);
    }

    public static String getResult() { return result; }
    public static boolean isLoaded() { return result.equals("CARREGADA COM SUCESSO"); }
}
