package com.maliopt.world;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.BufferBuilder;

public class NexusVBOInjector {

    public static void inject(ChunkBuilder.BuiltChunk chunk, VulkanResult result) {

        BufferBuilder.BuiltBuffer solid =
            new BufferBuilder.BuiltBuffer(result.solid, null);

        BufferBuilder.BuiltBuffer cutout =
            new BufferBuilder.BuiltBuffer(result.cutout, null);

        BufferBuilder.BuiltBuffer translucent =
            new BufferBuilder.BuiltBuffer(result.translucent, null);

        chunk.setBuffer(RenderLayer.getSolid(), solid);
        chunk.setBuffer(RenderLayer.getCutout(), cutout);
        chunk.setBuffer(RenderLayer.getTranslucent(), translucent);
    }
}
