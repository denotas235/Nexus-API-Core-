package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.nio.ByteBuffer;
import java.nio.file.Path;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin {

    @Inject(
        method = "read(Lnet/minecraft/client/texture/NativeImage$InternalFormat;Ljava/nio/ByteBuffer;)Lnet/minecraft/client/texture/NativeImage;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void onRead(
        NativeImage.InternalFormat format,
        ByteBuffer buffer,
        CallbackInfoReturnable<NativeImage> cir
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;
        if (!ASTCEncoder.isAvailable())        return;

        NativeImage image = cir.getReturnValue();
        if (image == null) return;

        try {
            int width  = image.getWidth();
            int height = image.getHeight();

            // Extrai RGBA8
            byte[] rgbaData = extractRGBA(image, width, height);
            if (rgbaData == null || rgbaData.length == 0) return;

            // Categoria DEFAULT — TextureManagerMixin vai refinar
            ASTCTextureCategory category = ASTCTextureCategory.DEFAULT;

            // Verifica cache THOROUGH
            Path cached = ASTCCache.getCached(rgbaData, category, true);

            // Verifica cache FASTEST
            if (cached == null) {
                cached = ASTCCache.getCached(rgbaData, category, false);
            }

            // Comprime FASTEST se não há cache
            if (cached == null) {
                byte[] astcFastest = ASTCEncoder.compress(
                    rgbaData, width, height, category, false
                );
                if (astcFastest != null) {
                    cached = ASTCCache.save(rgbaData, category, false, astcFastest);
                    BackgroundRecompressor.submit(rgbaData, width, height, category);
                }
            }

            // Enfileira para upload pelo TextureManagerMixin
            if (cached != null) {
                ASTCUploadQueue.enqueue(image, cached, width, height, category);
            }

        } catch (Exception e) {
            System.err.println("[NexusASTC] NativeImageMixin erro: " + e.getMessage());
        }
    }

    private static byte[] extractRGBA(NativeImage image, int width, int height) {
        try {
            byte[] data = new byte[width * height * 4];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getColor(x, y);
                    int i     = (y * width + x) * 4;
                    // NativeImage ABGR → RGBA
                    data[i]     = (byte)((pixel)       & 0xFF); // R
                    data[i + 1] = (byte)((pixel >> 8)  & 0xFF); // G
                    data[i + 2] = (byte)((pixel >> 16) & 0xFF); // B
                    data[i + 3] = (byte)((pixel >> 24) & 0xFF); // A
                }
            }
            return data;
        } catch (Exception e) {
            System.err.println("[NexusASTC] extractRGBA erro: " + e.getMessage());
            return null;
        }
    }
}
