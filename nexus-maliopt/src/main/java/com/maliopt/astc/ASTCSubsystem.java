package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;

/**
 * ASTCSubsystem — orchestrator da Fase ASTC
 *
 * Inicializa todos os componentes pela ordem correcta.
 * Chamado pelo MaliOptMod.onInitializeClient().
 */
public final class ASTCSubsystem {

    private static boolean initialized = false;
    private static boolean available = false;

    private ASTCSubsystem() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        MaliOptMod.LOGGER.info("[ASTC] ═══ Inicializando subsistema ASTC ═══");

        // Verifica se ASTC LDR está disponível
        if (!ExtensionActivator.hasAstcLdr) {
            MaliOptMod.LOGGER.warn("[ASTC] ASTC LDR não disponível — subsistema desactivado");
            return;
        }
        available = true;

        // 1. Encoder (JNI)
        ASTCEncoder.init();

        // 2. Cache em disco
        ASTCCacheManager.init();

        // 3. Upload GL
        ASTCTextureLoader.init();

        // 4. Decode hints (FP16 on-chip)
        ASTCDecodeHintManager.init();

        MaliOptMod.LOGGER.info("[ASTC] ✅ Subsistema ASTC pronto");
        MaliOptMod.LOGGER.info("[ASTC]    Encoder nativo: {}", ASTCEncoder.isNativeAvailable() ? "✅" : "❌ (fallback Java)");
        MaliOptMod.LOGGER.info("[ASTC]    Cache: {}", ASTCCacheManager.isReady() ? "✅" : "❌");
    }

    public static boolean isAvailable() {
        return available && initialized;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
