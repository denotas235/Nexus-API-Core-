package com.nexus.modules.tdbr.mixin;

import com.nexus.modules.tdbr.TDBRModule;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {

    // Antes do render, bind do nosso FBO
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter counter, boolean bl, Camera camera,
                               GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager,
                               Matrix4f matrix4f, Matrix4f matrix4f2, CallbackInfo ci) {
        TDBRModule module = TDBRModule.Companion.getModuleInstance();
        if (module != null) {
            module.bindFboAndClear();
        }
    }
}
