package com.nexus.textures;
import net.fabricmc.api.ClientModInitializer;
public class NexusTexturesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("[NexusTextures] ASTC Texture Module loaded.");
    }
}
