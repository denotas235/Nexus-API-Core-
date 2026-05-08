package com.maliopt.world;

import net.minecraft.client.render.chunk.RenderSection;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.BufferBuilder.BuiltBuffer;

public class NexusVBOInjector {

    public static void inject(RenderSection section, VulkanResult result) {

        BuiltBuffer solid = BuiltBuffer.fromByteBuffer(result.solid);
        BuiltBuffer cutout = BuiltBuffer.fromByteBuffer(result.cutout);
        BuiltBuffer translucent = BuiltBuffer.fromByteBuffer(result.translucent);

        section.setBuffer(RenderLayer.getSolid(), solid);
        section.setBuffer(RenderLayer.getCutout(), cutout);
        section.setBuffer(RenderLayer.getTranslucent(), translucent);
    }
}
