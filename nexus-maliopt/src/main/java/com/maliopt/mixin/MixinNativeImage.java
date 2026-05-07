package com.maliopt.mixin;

import com.maliopt.astc.ASTCTextureLoader;
import com.nexus.modules.textures.ASTCTextureRegistry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NativeImage.class)
public class MixinNativeImage {

    @Inject(method = "upload(IIIIIIIZZZZ)V", at = @At("HEAD"), cancellable = true, require = 1)
    private void onUpload(int level, int xOffset, int yOffset,
                          int skipPixels, int skipRows,
                          int width, int height,
                          boolean blur, boolean mipmap,
                          boolean close, boolean linear,
                          CallbackInfo ci) {
        if (level != 0 || !ASTCTextureLoader.isAvailable()) return;

        NativeImage self = (NativeImage)(Object) this;
        // Obtém o Identifier da textura a partir do NativeImage (via campo privado)
        Identifier id = self.getTextureIdentifier();   // método adicionado via AW ou mixin acessor
        if (id == null) return;

        byte[] astcData = ASTCTextureRegistry.getASTCData(id);
        if (astcData == null) return;   // só usa ASTC se existir ficheiro pré-comprimido

        // Lê o cabeçalho ASTC (16 bytes) para obter dimensões e tamanho do bloco
        if (astcData.length < 16) return;
        int blockX = astcData[7] & 0xFF;
        int blockY = astcData[8] & 0xFF;

        ASTCTextureLoader.upload(width, height, blockX, blockY, astcData);
        ci.cancel();   // cancela o upload vanilla
    }
}
