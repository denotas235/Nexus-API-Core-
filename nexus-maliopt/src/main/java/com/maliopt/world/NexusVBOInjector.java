package com.maliopt.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.chunk.BuiltBuffer;
import net.minecraft.client.render.chunk.BuiltBuffer.SortState;
import net.minecraft.util.math.Box;

public class NexusVBOInjector {

    public static SectionBuilder.RenderData createRenderData(VulkanResult result, int ox, int oy, int oz) {

        Map<RenderLayer, BuiltBuffer> map = new HashMap<>();

        map.put(RenderLayer.getSolid(), new BuiltBuffer(result.solid, new SortState()));
        map.put(RenderLayer.getCutout(), new BuiltBuffer(result.cutout, new SortState()));
        map.put(RenderLayer.getTranslucent(), new BuiltBuffer(result.translucent, new SortState()));

        SectionBuilder.TranslucencyData translucency =
            new SectionBuilder.TranslucencyData(RenderLayer.getTranslucent(), null);

        Box box = new Box(ox, oy, oz, ox + 16, oy + 16, oz + 16);

        ChunkOcclusionData occlusion = new ChunkOcclusionData();

        return new SectionBuilder.RenderData(map, translucency, box, occlusion);
    }
}
