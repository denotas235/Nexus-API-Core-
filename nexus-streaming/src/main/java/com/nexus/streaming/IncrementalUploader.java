package com.nexus.streaming;

import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;

/**
 * Envia apenas a regiao alterada de um buffer de chunk para a GPU.
 *
 * Modo 1 (buffer storage): escreve diretamente no ByteBuffer mapeado.
 *   Zero copias CPU→GPU — o driver le diretamente da memoria partilhada.
 *
 * Modo 2 (fallback): glBufferSubData com offset e tamanho da regiao alterada.
 *   Copia apenas a diferenca, evitando reenviar o chunk inteiro.
 */
public class IncrementalUploader {

    private final ChunkBufferManager bufferManager;

    public IncrementalUploader(ChunkBufferManager bm) {
        this.bufferManager = bm;
    }

    /**
     * Envia uma sub-regiao de vertices para a GPU.
     *
     * @param chunkKey  chave unica do chunk (de ChunkBufferManager.keyOf)
     * @param data      bytes novos a enviar
     * @param offset    offset em bytes dentro do buffer
     */
    public void uploadPartial(long chunkKey, byte[] data, int offset) {
        if (data == null || data.length == 0) return;

        ByteBuffer mapped = bufferManager.getOrMapBuffer(chunkKey);

        if (mapped != null) {
            // Modo rapido: escrita directa no buffer persistente
            mapped.position(offset);
            mapped.put(data, 0, data.length);
            mapped.position(0);
            // MAP_COHERENT: sem necessidade de glFlushMappedBufferRange
        } else {
            // Fallback: sub-upload via glBufferSubData
            int vbo = bufferManager.getOrCreateBuffer(chunkKey);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            ByteBuffer tmp = org.lwjgl.system.MemoryUtil.memAlloc(data.length);
            try {
                tmp.put(data).flip();
                GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, offset, tmp);
            } finally {
                org.lwjgl.system.MemoryUtil.memFree(tmp);
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            }
        }
    }

    /**
     * Realiza um upload de textura parcial (sub-regiao do atlas).
     * Usa GL_UNPACK_ROW_LENGTH para enviar apenas as linhas alteradas.
     *
     * @param textureId  ID da textura GL do atlas
     * @param x, y       canto superior esquerdo da regiao
     * @param w, h       dimensoes da regiao
     * @param pixels     dados RGBA da regiao
     */
    public void uploadTextureRegion(int textureId, int x, int y, int w, int h, ByteBuffer pixels) {
        if (pixels == null || textureId == 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glPixelStorei(GL20.GL_UNPACK_ROW_LENGTH, w);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, x, y, w, h,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glPixelStorei(GL20.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }
}