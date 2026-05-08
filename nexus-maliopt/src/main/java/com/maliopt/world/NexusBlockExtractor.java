package com.maliopt.world;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class NexusBlockExtractor {

    public static BlockData[] extract(ChunkRendererRegion region) {
        BlockData[] out = new BlockData[32 * 32 * 32];

        int i = 0;

        int cx = region.getCenterX();
        int cy = region.getCenterY();
        int cz = region.getCenterZ();

        var blockLight = region.getLightingProvider().get(LightType.BLOCK);
        var skyLight   = region.getLightingProvider().get(LightType.SKY);

        for (int x = -16; x < 16; x++) {
            for (int y = -16; y < 16; y++) {
                for (int z = -16; z < 16; z++) {

                    BlockPos pos = new BlockPos(cx + x, cy + y, cz + z);
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
