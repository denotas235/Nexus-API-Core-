package com.maliopt.world;

import com.maliopt.MaliOptMod;
import com.maliopt.gpu.ExtensionActivator;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * WorldCache — armazena geometria de chunks em buffers GL persistentes.
 * Usa GL_EXT_buffer_storage (via GL43) para permitir "congelar" dados na VRAM.
 * Quando o jogador revisita uma área, os buffers são reutilizados sem reconstrução.
 */
public class WorldCache {
    private static final Map<Long, Integer> chunkBuffers = new HashMap<>(); // posKey -> bufferId
    private static boolean hasBufferStorage = false;

    public static void init() {
        hasBufferStorage = ExtensionActivator.hasBufferStorage;
        MaliOptMod.LOGGER.info("[WorldCache] Buffer Storage: {}", hasBufferStorage ? "✅" : "❌");
    }

    /**
     * Armazena dados de geometria de um chunk.
     * @param chunkKey chave única do chunk (ex: ChunkPos.toLong)
     * @param data dados do vértice (posições, texturas)
     */
    public static int storeChunkGeometry(long chunkKey, ByteBuffer data) {
        // Remove buffer antigo se existir
        removeChunkGeometry(chunkKey);

        int bufferId = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, bufferId);
        if (hasBufferStorage) {
            // GL_EXT_buffer_storage: mapeável persistentemente
            GL43.glBufferStorage(GL30.GL_ARRAY_BUFFER, data, GL43.GL_MAP_WRITE_BIT | 0x0040);
        } else {
            // Fallback: glBufferData com GL_STATIC_DRAW
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, data, GL30.GL_STATIC_DRAW);
        }
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
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
            GL30.glDeleteBuffers(existing);
        }
    }

    public static void clearCache() {
        for (int buf : chunkBuffers.values()) {
            GL30.glDeleteBuffers(buf);
        }
        chunkBuffers.clear();
        MaliOptMod.LOGGER.info("[WorldCache] Cache limpo");
    }
}
