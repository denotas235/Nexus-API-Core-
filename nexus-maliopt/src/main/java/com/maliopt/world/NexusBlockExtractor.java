package com.maliopt.world;

import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

public class NexusBlockExtractor {

    public static BlockData[] extract(ChunkRendererRegion region) {
        BlockData[] out = new BlockData[32 * 32 * 32];

        int i = 0;

        int ox = region.getOrigin().getX();
        int oy = region.getOrigin().getY();
        int oz = region.getOrigin().getZ();

        var blockLight = region.getLightingProvider().get(LightType.BLOCK);
        var skyLight   = region.getLightingProvider().get(LightType.SKY);

        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 32; z++) {

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
