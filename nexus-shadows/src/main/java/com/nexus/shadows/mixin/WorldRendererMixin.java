package com.nexus.shadows.mixin;

import com.nexus.shadows.ShadowPipeline;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private void onRenderHead(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.world != null) {
            ShadowPipeline.renderShadowPass(mc);
        }
    }
}
