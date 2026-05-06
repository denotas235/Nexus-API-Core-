package com.maliopt.mixin;

import com.maliopt.astc.ASTCBlockSelector;
import com.maliopt.astc.ASTCCacheManager;
import com.maliopt.astc.ASTCDecodeHintManager;
import com.maliopt.astc.ASTCEncoder;
import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.astc.ASTCTextureLoader;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NativeImage.class)
public class MixinNativeImage {

    // Assinatura correcta para 1.21.1 — level, offsetX, offsetY, unpackSkipRows, unpackSkipPixels,
    // unpackRowLength, blur, clamp, close
    @Inject(
        method = "upload(IIIIIIZZZLcom/mojang/blaze3d/platform/NativeImage$InternalGlFormat;)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0  // não crashar se assinatura mudar
    )
    private void onUpload(int level, int offsetX, int offsetY,
                          int unpackSkipRows, int unpackSkipPixels, int unpackRowLength,
                          boolean blur, boolean clamp, boolean close,
                          NativeImage.InternalGlFormat format,
                          CallbackInfo ci) {

        // Só actua se o subsistema ASTC estiver pronto e for nível 0 (mipmap base)
        if (!ASTCSubsystem.isAvailable() || !ASTCTextureLoader.isAvailable()) return;
        if (level != 0) return;

        NativeImage self = (NativeImage) (Object) this;
        int w = self.getWidth();
        int h = self.getHeight();

        // Ignora texturas muito pequenas (ícones, cursores)
        if (w < 4 || h < 4) return;

        int[] block = ASTCBlockSelector.select("unknown");
        int bx = block[0];
        int by = block[1];

        // Coleta pixels RGBA
        byte[] rgba = new byte[w * h * 4];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = self.getColor(x, y);
                int idx = (y * w + x) * 4;
                rgba[idx]     = (byte)((pixel >> 16) & 0xFF); // R
                rgba[idx + 1] = (byte)((pixel >>  8) & 0xFF); // G
                rgba[idx + 2] = (byte)( pixel        & 0xFF); // B
                rgba[idx + 3] = (byte)((pixel >> 24) & 0xFF); // A
            }
        }

        String hash = ASTCCacheManager.hashImage(rgba, w, h);

        byte[] compressed = null;
        if (ASTCCacheManager.exists(hash, w, h, bx, by)) {
            compressed = ASTCCacheManager.load(hash, w, h, bx, by);
        } else {
            compressed = ASTCEncoder.compress(w, h, bx, by, rgba);
            if (compressed != null) {
                ASTCCacheManager.save(hash, w, h, bx, by, compressed);
            }
        }

        if (compressed != null) {
            ASTCDecodeHintManager.apply(false);
            ASTCTextureLoader.upload(w, h, bx, by, compressed);
            ci.cancel(); // cancela o upload vanilla — ASTC já está na GPU
        }
        // Se compress falhou, deixa o vanilla prosseguir normalmente
    }
}
