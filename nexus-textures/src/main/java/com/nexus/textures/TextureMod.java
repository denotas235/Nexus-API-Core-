package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureMod implements ClientModInitializer {
    public static final String MOD_ID = "nexus-textures";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[TextureMod] Module loading...");
        TextureModule.load();
        LOGGER.info("[TextureMod] {} ASTC textures loaded.", ASTCTextureRegistry.count());
    }
}
