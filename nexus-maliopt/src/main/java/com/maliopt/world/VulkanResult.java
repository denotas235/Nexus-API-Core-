package com.maliopt.world;

import java.nio.ByteBuffer;

public class VulkanResult {
    public final ByteBuffer solid;
    public final ByteBuffer cutout;
    public final ByteBuffer translucent;

    public VulkanResult(ByteBuffer solid, ByteBuffer cutout, ByteBuffer translucent) {
        this.solid = solid;
        this.cutout = cutout;
        this.translucent = translucent;
    }
}
