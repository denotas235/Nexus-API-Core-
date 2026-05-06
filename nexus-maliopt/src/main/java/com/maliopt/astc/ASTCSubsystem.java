package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;
import net.minecraft.client.texture.NativeImage;

public final class ASTCSubsystem {

    private static boolean initialized = false;
    private static boolean available   = false;

    private ASTCSubsystem() {}

    public static void init() {
        if (initialized) return;
        initialized = true;

        MaliOptMod.LOGGER.info("[ASTC] ═══ Inicializando subsistema ASTC ═══");

        if (!ExtensionActivator.hasAstcLdr) {
            MaliOptMod.LOGGER.warn("[ASTC] ASTC LDR não disponível — subsistema desactivado");
            return;
        }
        available = true;

        ASTCEncoder.init();
        ASTCCacheManager.init();
        ASTCTextureLoader.init();
        ASTCDecodeHintManager.init();

        MaliOptMod.LOGGER.info("[ASTC] ✅ Subsistema ASTC pronto");
        MaliOptMod.LOGGER.info("[ASTC]    Encoder nativo: {}", ASTCEncoder.isNativeAvailable() ? "✅" : "❌");
        MaliOptMod.LOGGER.info("[ASTC]    Cache: {}", ASTCCacheManager.isReady() ? "✅" : "❌");
    }

    /**
     * Chamado pelo MixinNativeImage.
     * @return true se o upload ASTC foi feito — mixin deve cancelar o vanilla
     */
    public static boolean handleUpload(int level, int x, int y,
                                       int width, int height,
                                       boolean blur, boolean mipmap,
                                       boolean close, boolean linear) {
        if (!available || !ASTCTextureLoader.isAvailable()) return false;
        if (level != 0) return false;
        if (width < 4 || height < 4) return false;

        int[] block = ASTCBlockSelector.select("unknown");
        int bx = block[0];
        int by = block[1];

        // Não temos acesso ao NativeImage aqui — os pixels são lidos no Mixin
        // handleUpload só decide se deve processar; a lógica de pixels fica no Mixin
        // Retorna false para deixar o Mixin fazer o trabalho completo
        return false;
    }

    /**
     * Versão completa — recebe os pixels já lidos pelo Mixin.
     * @return true se upload ASTC foi feito com sucesso
     */
    public static boolean handleUpload(int level, int width, int height,
                                       byte[] rgba) {
        if (!available || !ASTCTextureLoader.isAvailable()) return false;
        if (level != 0) return false;
        if (width < 4 || height < 4) return false;

        int[] block = ASTCBlockSelector.select("unknown");
        int bx = block[0];
        int by = block[1];

        String hash = ASTCCacheManager.hashImage(rgba, width, height);

        byte[] compressed = null;
        if (ASTCCacheManager.exists(hash, width, height, bx, by)) {
            compressed = ASTCCacheManager.load(hash, width, height, bx, by);
        } else {
            compressed = ASTCEncoder.compress(width, height, bx, by, rgba);
            if (compressed != null) {
                ASTCCacheManager.save(hash, width, height, bx, by, compressed);
            }
        }

        if (compressed != null) {
            ASTCDecodeHintManager.apply(false);
            ASTCTextureLoader.upload(width, height, bx, by, compressed);
            MaliOptMod.LOGGER.debug("[ASTC] ✅ {}x{} block={}x{} hash={}", width, height, bx, by, hash.substring(0, 8));
            return true;
        }
        return false;
    }

    public static boolean isAvailable()   { return available && initialized; }
    public static boolean isInitialized() { return initialized; }
}
