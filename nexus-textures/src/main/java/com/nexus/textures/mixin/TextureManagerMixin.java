package com.nexus.textures.mixin;

import com.nexus.textures.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    // Intercepta o registo de texturas para capturar o path real
    @Inject(
        method = "registerTexture",
        at = @At("HEAD")
    )
    private void onRegisterTexture(
        Identifier id,
        AbstractTexture texture,
        CallbackInfo ci
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;

        String path = id.toString();

        // Refina categoria com path real
        ASTCTextureCategory category = ASTCTextureCategory.fromPath(path);

        // Actualiza categoria na fila de upload
        ASTCUploadQueue.updateCategory(texture, category);
    }

    // Intercepta o bind de textura para fazer upload ASTC
    @Inject(
        method = "bindTexture",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onBindTexture(
        Identifier id,
        CallbackInfo ci
    ) {
        if (!ASTCDecodeMode.isASTCSupported()) return;

        // Verifica se há upload ASTC pendente para esta textura
        ASTCUploadQueue.Entry entry = ASTCUploadQueue.pollByIdentifier(id.toString());
        if (entry == null) return;

        byte[] astcData = ASTCCache.load(entry.cachePath);
        if (astcData == null) return;

        // Faz upload ASTC via PBO
        int textureId = entry.texture.getGlId();
        ASTCUploader.upload(
            textureId,
            entry.width,
            entry.height,
            entry.category,
            astcData
        );

        // Cancela bind normal — já fizemos o upload
        ci.cancel();
    }
}
