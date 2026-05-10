package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(NativeImage.class)
public abstract class NativeImageMixin {

    // Intercepta upload GL da NativeImage — método correcto em 1.21.1
    @Inject(
        method = "upload(IIIIIZZZZLnet/minecraft/client/texture/NativeImage$InternalFormat;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onUpload(
        int level,
        int offsetX,
        int offsetY,
        int width,
        int height,
        boolean mipmap,
        boolean linearFiltering,
        boolean clamp,
        boolean close,
        NativeImage.InternalFormat format,
        CallbackInfo ci
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;
        if (!ASTCEncoder.isAvailable())        return;

        NativeImage self = (NativeImage)(Object)this;

        try {
            int imgWidth  = self.getWidth();
            int imgHeight = self.getHeight();

            // Só processa upload completo (level 0, sem offset)
            if (level != 0 || offsetX != 0 || offsetY != 0) return;

            byte[] rgbaData = extractRGBA(self, imgWidth, imgHeight);
            if (rgbaData == null) return;

            ASTCTextureCategory category = ASTCTextureCategory.DEFAULT;

            // Verifica cache THOROUGH
            java.nio.file.Path cached = ASTCCache.getCached(rgbaData, category, true);

            // Verifica cache FASTEST
            if (cached == null) {
                cached = ASTCCache.getCached(rgbaData, category, false);
            }

            // Comprime FASTEST se sem cache
            if (cached == null) {
                byte[] astcFastest = ASTCEncoder.compress(
                    rgbaData, imgWidth, imgHeight, category, false
                );
                if (astcFastest != null) {
                    cached = ASTCCache.save(rgbaData, category, false, astcFastest);
                }
            }

            if (cached == null) return;

            // Enfileira para upload — TextureManagerMixin vai refinar categoria
            ASTCUploadQueue.enqueue(self, cached, imgWidth, imgHeight, category, rgbaData);

            // Obtém GL ID actual e faz upload imediato
            int glId = GL11GetCurrentTexture();
            if (glId > 0) {
                byte[] astcData = ASTCCache.load(cached);
                if (astcData != null) {
                    ASTCUploader.upload(glId, imgWidth, imgHeight, category, astcData);
                    ci.cancel(); // cancela upload vanilla
                }
            }

        } catch (Exception e) {
            System.err.println("[NexusASTC] NativeImageMixin erro: " + e.getMessage());
            // Não cancela — deixa vanilla fazer upload
        }
    }

    private static int GL11GetCurrentTexture() {
        try {
            return org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D);
        } catch (Exception e) {
            return -1;
        }
    }

    private static byte[] extractRGBA(NativeImage image, int width, int height) {
        try {
            byte[] data = new byte[width * height * 4];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getColor(x, y);
                    int i     = (y * width + x) * 4;
                    data[i]     = (byte)( pixel        & 0xFF); // R
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
