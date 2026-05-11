package com.nexus.streaming;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.*;

/**
 * Inicializa o pipeline de streaming incremental.
 *
 * Extensoes GLES utilizadas:
 *   GL_EXT_buffer_storage  → buffers persistentes (GL44 em desktop)
 *   GL_EXT_map_buffer_range → mapeamento parcial (GL30 em desktop)
 *   GL_EXT_unpack_subimage → upload de sub-regioes de textura
 */
public class StreamingPipeline {

    private static boolean initialized        = false;
    private static boolean hasBufferStorage   = false;
    private static boolean hasMapBufferRange  = false;
    private static boolean hasUnpackSubimage  = false;

    private static ChunkBufferManager bufferManager;
    private static IncrementalUploader uploader;

    public static void initGL() {
        if (initialized) return;
        try {
            GLCapabilities caps = GL.getCapabilities();

            // GL_EXT_buffer_storage / OpenGL 4.4
            hasBufferStorage  = caps.GL_ARB_buffer_storage || caps.OpenGL44;
            // GL_EXT_map_buffer_range / OpenGL 3.0
            hasMapBufferRange = caps.GL_ARB_map_buffer_range || caps.OpenGL30;
            // GL_EXT_unpack_subimage (GLES 3.0 / desktop sempre disponivel)
            hasUnpackSubimage = caps.OpenGL12; // GL_UNPACK_ROW_LENGTH disponivel desde GL 1.2

            bufferManager = new ChunkBufferManager();
            bufferManager.init(hasBufferStorage && hasMapBufferRange);

            uploader = new IncrementalUploader(bufferManager);
            UploadQueue.init(uploader);

            initialized = true;

            NexusStreamingClient.LOGGER.info("[Streaming] Pipeline GL pronto:");
            NexusStreamingClient.LOGGER.info("[Streaming]   BufferStorage:  {}", hasBufferStorage  ? "ON" : "OFF (fallback glBufferSubData)");
            NexusStreamingClient.LOGGER.info("[Streaming]   MapBufferRange: {}", hasMapBufferRange ? "ON" : "OFF");
            NexusStreamingClient.LOGGER.info("[Streaming]   UnpackSubimage: {}", hasUnpackSubimage ? "ON" : "OFF");

        } catch (Exception e) {
            NexusStreamingClient.LOGGER.error("[Streaming] initGL falhou: {}", e.getMessage());
        }
    }

    public static boolean isInitialized()       { return initialized; }
    public static boolean hasBufferStorage()    { return hasBufferStorage; }
    public static boolean hasMapBufferRange()   { return hasMapBufferRange; }
    public static boolean hasUnpackSubimage()   { return hasUnpackSubimage; }
    public static ChunkBufferManager getBufferManager() { return bufferManager; }
    public static IncrementalUploader getUploader()     { return uploader; }
}