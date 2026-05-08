package com.maliopt.world;

import net.minecraft.block.BlockState;

public class BlockData {
    public final BlockState state;
    public final int blockLight;
    public final int skyLight;

    public BlockData(BlockState state, int blockLight, int skyLight) {
        this.state = state;
        this.blockLight = blockLight;
        this.skyLight = skyLight;
    }
}
