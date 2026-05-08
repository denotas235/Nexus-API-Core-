package com.maliopt.world;

public class VulkanWorker {

    public static VulkanResult submitChunk(BlockData[] blocks) {
        return new VulkanResult(
            java.nio.ByteBuffer.allocateDirect(1),
            java.nio.ByteBuffer.allocateDirect(1),
            java.nio.ByteBuffer.allocateDirect(1)
        );
    }
}
