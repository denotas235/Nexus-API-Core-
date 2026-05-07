package com.maliopt.world;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Atualiza apenas a parte alterada de um chunk, em vez de reenviar tudo.
 * Usa GL_EXT_map_buffer_range para uploads parciais.
 */
public class MeshDiffEngine {
    private static final Map<Long, ByteBuffer> chunkMeshes = new HashMap<>();
    private static boolean mapBufferRangeAvailable = false;

    public static void init() {
        mapBufferRangeAvailable = com.maliopt.gpu.ExtensionActivator.hasBufferStorage;
        MaliOptMod.LOGGER.info("[MeshDiff] GL_EXT_map_buffer_range: {}", mapBufferRangeAvailable ? "✅" : "❌");
    }

    /**
     * Atualiza parcialmente um chunk. Se a mesh anterior existir, só altera os bytes diferentes.
     * Caso contrário, faz upload completo.
     */
    public static void partialUpload(long chunkKey, int vbo, ByteBuffer newMesh) {
        ByteBuffer oldMesh = chunkMeshes.get(chunkKey);
        if (oldMesh == null || !mapBufferRangeAvailable) {
            fullUpload(vbo, newMesh);
        } else if (oldMesh.capacity() == newMesh.capacity()) {
            // Upload só das partes diferentes
            for (int i = 0; i < newMesh.capacity(); i++) {
                if (newMesh.get(i) != oldMesh.get(i)) {
                    // Encontra o fim deste segmento diferente
                    int end = i;
                    while (end < newMesh.capacity() && newMesh.get(end) != oldMesh.get(end)) end++;
                    int length = end - i;
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
                    GL30.glBufferSubData(GL15.GL_ARRAY_BUFFER, i, newMesh.slice(i, length));
                    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                    i = end;
                }
            }
        } else {
            // Tamanhos diferentes = upload completo
            fullUpload(vbo, newMesh);
        }
        chunkMeshes.put(chunkKey, newMesh);
    }

    private static void fullUpload(int vbo, ByteBuffer mesh) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, mesh, GL15.GL_DYNAMIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
}
