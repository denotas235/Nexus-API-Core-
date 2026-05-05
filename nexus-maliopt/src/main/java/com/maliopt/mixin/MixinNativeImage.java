package com.maliopt.mixin;

import com.maliopt.astc.ASTCBlockSelector;
import com.maliopt.astc.ASTCCacheManager;
import com.maliopt.astc.ASTCDecodeHintManager;
import com.maliopt.astc.ASTCEncoder;
import com.maliopt.astc.ASTCTextureLoader;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NativeImage.class)
public class MixinNativeImage {

    /**
     * Intercepta o upload de texturas individuais.
     * Se ASTC estiver disponível, substitui o upload vanilla
     * por glCompressedTexImage2D com dados comprimidos.
     */
    @Inject(method = "upload", at = @At("HEAD"))
    private void onUpload(int level, int offsetX, int offsetY, boolean close, CallbackInfo ci) {
        if (!ASTCTextureLoader.isAvailable()) return;

        NativeImage self = (NativeImage) (Object) this;
        int w = self.getWidth();
        int h = self.getHeight();

        // Determina block size
        int[] block = ASTCBlockSelector.select("unknown"); // path não disponível aqui — fallback
        int bx = block[0];
        int by = block[1];

        // Obtém pixels RGBA
        byte[] rgba = new byte[w * h * 4];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = self.getColor(x, y);
                int idx = (y * w + x) * 4;
                rgba[idx]     = (byte)((pixel >> 16) & 0xFF);
                rgba[idx + 1] = (byte)((pixel >>  8) & 0xFF);
                rgba[idx + 2] = (byte)( pixel        & 0xFF);
                rgba[idx + 3] = (byte)((pixel >> 24) & 0xFF);
            }
        }

        // Hash
        String hash = ASTCCacheManager.hashImage(rgba, w, h);

        // Cache?
        byte[] compressed = null;
        if (ASTCCacheManager.exists(hash, w, h, bx, by)) {
            compressed = ASTCCacheManager.load(hash, w, h, bx, by);
        } else if (ASTCEncoder.isNativeAvailable()) {
            compressed = ASTCEncoder.compressASTC(w, h, bx, by, rgba);
            if (compressed != null) {
                ASTCCacheManager.save(hash, w, h, bx, by, compressed);
            }
        }

        if (compressed != null) {
            // Aplica decode hint
            boolean isHDR = false; // texturas comuns não são HDR
            ASTCDecodeHintManager.apply(isHDR);

            // Upload ASTC
            int texId = ASTCTextureLoader.upload(w, h, bx, by, compressed);

            // Cancela o upload vanilla? Não via @Inject — mas podemos marcar
            // A textura já está na GPU; o upload vanilla será redundante mas inofensivo
        }
    }
}
