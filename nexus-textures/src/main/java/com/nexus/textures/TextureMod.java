package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
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

        if (ASTCEncoder.isAvailable()) {
            LOGGER.info("[TextureMod] astcenc-neon (ARM64) ativo — resource packs externos serao comprimidos ASTC em runtime.");
        } else {
            LOGGER.warn("[TextureMod] astcenc-neon NAO encontrado no JAR — resource packs externos usarao textures vanilla. {}",
                    ASTCEncoder.getLoadError());
        }

        // Listener de resource packs: limpa cache ao carregar novo pack
        // para que texturas de packs externos sejam re-comprimidas ASTC automaticamente
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
                .registerReloadListener(new NexusASTCReloadListener());

        // Barra de carregamento ASTC no HUD (visivel 6 segundos apos o carregamento)
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
            // Barra vazia
            drawContext.fill(x, y, x + barW, y + barH, 0x44FFFFFF);
            // Barra preenchida
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