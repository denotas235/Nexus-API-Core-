package com.maliopt.world;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class WorldCache {
    private static final Map<Long, Integer> chunkBuffers = new HashMap<>();
    private static boolean hasBufferStorage = false;

    public static void init() {
        hasBufferStorage = ExtensionActivator.hasBufferStorage;
        MaliOptMod.LOGGER.info("[WorldCache] Buffer Storage: {}", hasBufferStorage ? "✅" : "❌");
    }

    public static int storeChunkGeometry(long chunkKey, ByteBuffer data) {
        removeChunkGeometry(chunkKey);

        int bufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
        
        if (hasBufferStorage) {
            // Flags literais: 0x0010 = GL_MAP_WRITE_BIT, 0x0040 = GL_MAP_PERSISTENT_BIT
            GL43.glBufferStorage(GL15.GL_ARRAY_BUFFER, data, 0x0010 | 0x0040);
        } else {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        chunkBuffers.put(chunkKey, bufferId);
        return bufferId;
    }

    public static void removeChunkGeometry(long chunkKey) {
        Integer existing = chunkBuffers.remove(chunkKey);
        if (existing != null && existing >= 0) {
            GL15.glDeleteBuffers(existing);
        }
    }

    public static void clearCache() {
        for (int buf : chunkBuffers.values()) {
            GL15.glDeleteBuffers(buf);
        }
        chunkBuffers.clear();
        MaliOptMod.LOGGER.info("[WorldCache] Cache limpo");
    }
}
