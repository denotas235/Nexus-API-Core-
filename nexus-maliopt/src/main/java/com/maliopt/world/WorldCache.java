package com.maliopt.world;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.GL15;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class WorldCache {
    private static final Map<Long, Integer> chunkBuffers = new HashMap<>();

    public static void init() {
        MaliOptMod.LOGGER.info("[WorldCache] Inicializado (GL15)");
    }

    public static int storeChunkGeometry(long chunkKey, ByteBuffer data) {
        removeChunkGeometry(chunkKey);
        int bufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        chunkBuffers.put(chunkKey, bufferId);
        MaliOptMod.LOGGER.debug("[WorldCache] Chunk {} armazenado (buffer {})", chunkKey, bufferId);
        return bufferId;
    }

    public static int getChunkBuffer(long chunkKey) {
        return chunkBuffers.getOrDefault(chunkKey, -1);
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
