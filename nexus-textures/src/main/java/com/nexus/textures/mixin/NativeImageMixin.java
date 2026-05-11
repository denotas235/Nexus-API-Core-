package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * NativeImageMixin — interceta uploads de textura para substituir por ASTC.
 *
 * Fix 1: require=0 (era require=1) — evita crash se a assinatura do metodo
 *        upload() mudar numa versao menor do Minecraft.
 *
 * Fix 2: uploadASTC() ja nao cria uma nova textura GL orfa. Carrega os dados
 *        ASTC directamente para a textura actualmente ligada (a que o Minecraft
 *        criou e esta a tentar carregar), usando glCompressedTexImage2D sem
 *        chamar glGenTextures. Desta forma o ASTC e realmente aplicado.
 */
@Mixin(NativeImage.class)
public class NativeImageMixin {

    @Inject(method = "upload(IIIIIIIZZZZ)V", at = @At("HEAD"), cancellable = true, require = 0)
    private void onUpload(int level, int xOffset, int yOffset,
                          int skipPixels, int skipRows,
                          int width, int height,
                          boolean blur, boolean mipmap,
                          boolean close, boolean linear,
                          CallbackInfo ci) {
        // Apenas substituir uploads do nivel mip 0 e dimensoes validas para ASTC
        if (level != 0 || width < 4 || height < 4) return;

        String path = TexturePathTracker.getCurrentPath();
        if (path == null) return;

        // Verificar cache ASTC primeiro (pre-comprimido ou runtime)
        byte[] astc = ASTCCache.get(path);
        if (astc == null) {
            // Tentar o registry pre-compilado (manifest JAR)
            astc = ASTCTextureRegistry.get(path);
        }

        if (astc != null && astc.length >= 16) {
            uploadAstcToCurrent(astc, width, height);
            ASTCLoadingState.trackRuntimeUpload();
            ci.cancel();
            return;
        }

        // Sem ASTC disponivel — tentar comprimir em runtime
        NativeImage self = (NativeImage)(Object) this;
        byte[] rgba = new byte[width * height * 4];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = self.getColor(x, y);
                int idx = (y * width + x) * 4;
                rgba[idx]     = (byte)((pixel >> 16) & 0xFF); // R
                rgba[idx + 1] = (byte)((pixel >>  8) & 0xFF); // G
                rgba[idx + 2] = (byte)( pixel        & 0xFF); // B
                rgba[idx + 3] = (byte)((pixel >> 24) & 0xFF); // A
            }
        }

        ASTCTextureCategory cat = ASTCTextureCategory.fromPath(path);
        byte[] compressed = ASTCEncoder.compress(rgba, width, height, cat, false);
        if (compressed != null && compressed.length >= 16) {
            ASTCCache.put(path, compressed);
            uploadAstcToCurrent(compressed, width, height);
            ASTCLoadingState.trackRuntimeUpload();
            ci.cancel();
            // Agendar re-compressao de alta qualidade em background
            BackgroundRecompressor.schedule(path, rgba, width, height, cat);
        }
        // Falha de compressao — deixar o upload vanilla seguir normalmente
    }

    /**
     * Carrega dados ASTC para a textura GL actualmente ligada.
     * NAO cria uma nova textura — usa a textura do contexto actual do Minecraft.
     */
    private static void uploadAstcToCurrent(byte[] astcData, int width, int height) {
        if (astcData.length < 16) return;

        // Cabecalho ASTC: bytes 4-5 = block width/height
        int blockX = astcData[4] & 0xFF;
        int blockY = astcData[5] & 0xFF;

        int glFormat;
        if      (blockX == 4  && blockY == 4 ) glFormat = 0x93B0; // ASTC 4x4
        else if (blockX == 5  && blockY == 5 ) glFormat = 0x93B3; // ASTC 5x5
        else if (blockX == 8  && blockY == 8 ) glFormat = 0x93B7; // ASTC 8x8
        else if (blockX == 10 && blockY == 10) glFormat = 0x93BD; // ASTC 10x10
        else if (blockX == 12 && blockY == 12) glFormat = 0x93BF; // ASTC 12x12
        else                                   glFormat = 0x93B0; // fallback 4x4

        ByteBuffer buf = ByteBuffer.allocateDirect(astcData.length - 16);
        buf.put(astcData, 16, astcData.length - 16);
        buf.flip();

        // Carrega para a textura actualmente ligada — SEM glGenTextures!
        GL13.glCompressedTexImage2D(GL11.GL_TEXTURE_2D, 0, glFormat, width, height, 0, buf);
    }
}

