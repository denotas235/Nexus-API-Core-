package com.nexus.streaming;

import net.minecraft.util.math.ChunkPos;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gere buffers GL persistentes (GL_EXT_buffer_storage / GL44) por chunk.
 * Um buffer persistente e alocado uma unica vez e reutilizado em todos
 * os uploads parciais, eliminando realocacoes e driver stalls.
 */
public class ChunkBufferManager {

    // GL44 flags: MAP_WRITE | MAP_PERSISTENT | MAP_COHERENT
    private static final int PERSISTENT_FLAGS =
            GL30.GL_MAP_WRITE_BIT | GL44.GL_MAP_PERSISTENT_BIT | GL44.GL_MAP_COHERENT_BIT;

    // Tamanho inicial de cada buffer de chunk (vertices: 64 KB)
    private static final int CHUNK_BUFFER_SIZE = 64 * 1024;

    // chunkKey -> glBufferId
    private final ConcurrentHashMap<Long, Integer> buffers = new ConcurrentHashMap<>();
    // chunkKey -> ByteBuffer mapeado (persistente)
    private final ConcurrentHashMap<Long, ByteBuffer> mappedBuffers = new ConcurrentHashMap<>();

    private boolean supportsBufferStorage = false;

    public void init(boolean hasBufferStorage) {
        this.supportsBufferStorage = hasBufferStorage;
        NexusStreamingClient.LOGGER.info("[Streaming] ChunkBufferManager pronto (bufferStorage={})", hasBufferStorage);
    }

    /** Obtem ou cria o buffer GL para este chunk. */
    public int getOrCreateBuffer(long chunkKey) {
        return buffers.computeIfAbsent(chunkKey, k -> allocate());
    }

    private int allocate() {
        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        if (supportsBufferStorage) {
            // Buffer imutavel e persistentemente mapeavel
            GL44.glBufferStorage(GL15.GL_ARRAY_BUFFER, CHUNK_BUFFER_SIZE, PERSISTENT_FLAGS);
        } else {
            // Fallback: buffer mutable padrao
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, CHUNK_BUFFER_SIZE, GL15.GL_DYNAMIC_DRAW);
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        return vbo;
    }

    /** Obtem ou cria o mapeamento persistente para este buffer. */
    public ByteBuffer getOrMapBuffer(long chunkKey) {
        return mappedBuffers.computeIfAbsent(chunkKey, k -> {
            int vbo = getOrCreateBuffer(k);
            if (!supportsBufferStorage) return null;
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            ByteBuffer mapped = GL30.glMapBufferRange(
                GL15.GL_ARRAY_BUFFER, 0, CHUNK_BUFFER_SIZE,
                PERSISTENT_FLAGS,
                null
            );
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            return mapped;
        });
    }

    /** Liberta o buffer de um chunk quando ele sai do render distance. */
    public void free(long chunkKey) {
        Integer vbo = buffers.remove(chunkKey);
        if (vbo != null) {
            if (supportsBufferStorage && mappedBuffers.containsKey(chunkKey)) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            }
            GL15.glDeleteBuffers(vbo);
        }
        mappedBuffers.remove(chunkKey);
    }

    public static long keyOf(int cx, int cy, int cz) {
        return ((long)(cx & 0xFFFFF) << 40) | ((long)(cy & 0xFFFFF) << 20) | (cz & 0xFFFFF);
    }

    public static long keyOf(ChunkPos pos, int sectionY) {
        return keyOf(pos.x, sectionY, pos.z);
    }
}