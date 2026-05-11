package com.nexus.textures;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class NexusASTCReloadListener implements SimpleSynchronousResourceReloadListener {

    @Override
    public Identifier getFabricId() {
        return Identifier.of("nexus-textures", "astc-pack-reload");
    }

    @Override
    public void reload(ResourceManager manager) {
        int antes = ASTCCache.size();
        ASTCCache.clear();
        ASTCLoadingState.resetRuntimeUploads();
        TextureMod.LOGGER.info(
            "[NexusASTC] Resource pack recarregado — cache ASTC limpo ({} entradas). "
            + "Texturas de packs externos serao comprimidas ASTC em runtime via astcenc-neon.",
            antes
        );
    }
}