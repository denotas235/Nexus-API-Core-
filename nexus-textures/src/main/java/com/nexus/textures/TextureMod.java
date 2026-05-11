package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureMod implements ClientModInitializer {
    public static final String MOD_ID = "nexus-textures";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[TextureMod] Module loading...");
        TextureModule.load();
        LOGGER.info("[TextureMod] {} ASTC textures loaded in {}ms.",
                ASTCTextureRegistry.count(), ASTCLoadingState.getLoadTimeMs());

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            if (!ASTCLoadingState.shouldShowHud()) return;

            int sw = drawContext.getScaledWindowWidth();
            int sh = drawContext.getScaledWindowHeight();
            int barW = 260;
            int barH = 6;
            int x = (sw - barW) / 2;
            int y = sh - 28;

            float progress = ASTCLoadingState.getProgress();
            int filled = (int)(barW * progress);

            // Fundo semi-transparente
            drawContext.fill(x - 2, y - 16, x + barW + 2, y + barH + 3, 0xAA000000);
            // Barra de progresso
            drawContext.fill(x, y, x + barW, y + barH, 0x44FFFFFF);
            drawContext.fill(x, y, x + filled, y + barH, 0xFF22CC55);

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.textRenderer != null) {
                boolean done = ASTCLoadingState.isDone();
                String label = done
                    ? "Nexus ASTC  ON  \u2502  " + ASTCLoadingState.getLoaded() + " texturas ASTC ativas"
                    : "Nexus ASTC  carregando...  " + ASTCLoadingState.getLoaded() + "/" + ASTCLoadingState.getTotal();
                drawContext.drawText(mc.textRenderer, label, x, y - 13, 0xFF55FF55, true);
            }
        });
    }
}