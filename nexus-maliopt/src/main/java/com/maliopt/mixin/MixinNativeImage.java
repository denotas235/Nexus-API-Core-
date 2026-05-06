package com.maliopt.mixin;

import com.maliopt.astc.ASTCSubsystem;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NativeImage.class)
public class MixinNativeImage {

    @Inject(
        method = "upload(IIIIIIZZZZ)V",
        at = @At("HEAD"),
        cancellable = true,
        require = 0
    )
    private void onUpload(int level, int x, int y,
                          int skipPixels, int skipRows,
                          int width, int height,
                          boolean blur, boolean mipmap,
                          boolean close, boolean linear,
                          CallbackInfo ci) {

        if (!ASTCSubsystem.isAvailable()) return;
        if (level != 0 || width < 4 || height < 4) return;

        NativeImage self = (NativeImage)(Object) this;

        // Lê pixels RGBA
        byte[] rgba = new byte[width * height * 4];
        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int pixel = self.getColor(px, py);
                int idx = (py * width + px) * 4;
                rgba[idx]     = (byte)((pixel >> 16) & 0xFF);
                rgba[idx + 1] = (byte)((pixel >>  8) & 0xFF);
                rgba[idx + 2] = (byte)( pixel        & 0xFF);
                rgba[idx + 3] = (byte)((pixel >> 24) & 0xFF);
            }
        }

        if (ASTCSubsystem.handleUpload(level, width, height, rgba)) {
            ci.cancel();
        }
    }
}
