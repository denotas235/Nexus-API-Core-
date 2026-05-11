package com.nexuapicore.client.mixin;

import com.maliopt.pipeline.ShadowPass;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * WorldRendererMixin — aplica o shadow map (PCF) como post-process
 * DEPOIS de o WorldRenderer terminar de renderizar o mundo (TAIL),
 * garantindo que as sombras sao compostas em cima de toda a geometria.
 *
 * Bug original: a chamada estava em HEAD, executando antes de qualquer geometria
 * ter sido desenhada e sem nenhum applyToScreen() que aplicasse o resultado.
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Inject(
        method = "render",
        at = @At("TAIL")
    )
    private void nexus_applyPostProcessShadows(
        net.minecraft.client.render.RenderTickCounter tickCounter,
        boolean renderBlockOutline,
        Camera camera,
        GameRenderer gameRenderer,
        LightmapTextureManager lightmapTextureManager,
        Matrix4f matrix4f,
        Matrix4f matrix4f2,
        CallbackInfo ci
    ) {
        try {
            if (ShadowPass.isReady()) {
                ShadowPass.applyToScreen();
            }
        } catch (Throwable t) {
            // Nao crashar o jogo por causa do shadow post-process
        }
    }
}

