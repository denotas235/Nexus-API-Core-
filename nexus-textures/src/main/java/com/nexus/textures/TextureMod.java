package com.nexus.textures;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextureMod implements ClientModInitializer {
    public static final String MOD_ID = "nexus-textures";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        TextureModule.load();
        LOGGER.info("[Textures] ASTC texture pack active — {} textures registered.",
            ASTCTextureRegistry.count());
    }
}
