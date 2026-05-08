package com.maliopt.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.chunk.ChunkOcclusionData;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.BuiltBuffer.SortState;
import net.minecraft.client.render.IndexType;
import net.minecraft.util.math.Box;
import org.joml.Vector3f;

public class NexusVBOInjector {

    public static SectionBuilder.RenderData createRenderData(VulkanResult result, int ox, int oy, int oz) {

        Map<RenderLayer, BuiltBuffer> map = new HashMap<>();

        SortState sort = new SortState(new Vector3f[0], IndexType.NONE);

        if (result.solid != null)
            map.put(RenderLayer.getSolid(), new BuiltBuffer(result.solid, sort));

        if (result.cutout != null)
            map.put(RenderLayer.getCutout(), new BuiltBuffer(result.cutout, sort));

        if (result.translucent != null)
            map.put(RenderLayer.getTranslucent(), new BuiltBuffer(result.translucent, sort));

        Box box = new Box(ox, oy, oz, ox + 16, oy + 16, oz + 16);
        ChunkOcclusionData occlusion = new ChunkOcclusionData();

        return new SectionBuilder.RenderData(map, null, box, occlusion);
    }
}
