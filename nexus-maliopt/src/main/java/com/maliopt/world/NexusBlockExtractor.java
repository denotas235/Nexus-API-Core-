package com.maliopt.world;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.util.math.ChunkSectionPos;

public class NexusBlockExtractor {

    public static BlockData[] extract(ChunkRendererRegion region, ChunkSectionPos sectionPos) {

        BlockData[] out = new BlockData[18 * 18 * 18];
        int i = 0;

        int ox = sectionPos.getMinX();
        int oy = sectionPos.getMinY();
        int oz = sectionPos.getMinZ();

        var blockLight = region.getLightingProvider().get(LightType.BLOCK);
        var skyLight   = region.getLightingProvider().get(LightType.SKY);

        for (int x = -1; x <= 16; x++) {
            for (int y = -1; y <= 16; y++) {
                for (int z = -1; z <= 16; z++) {

                    BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    BlockState state = region.getBlockState(pos);

                    int bl = blockLight.getLightLevel(pos);
                    int sl = skyLight.getLightLevel(pos);

                    out[i++] = new BlockData(state, bl, sl);
                }
            }
        }

        return out;
    }
}
