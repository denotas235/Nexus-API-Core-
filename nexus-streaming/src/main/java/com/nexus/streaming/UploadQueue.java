package com.nexus.streaming;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fila thread-safe de pedidos de upload.
 *
 * A render thread enfileira pedidos; processTick() consume N por tick,
 * garantindo que a render thread nunca bloqueia.
 *
 * Fluxo:
 *   1. ChunkBuilderMixin.onRebuild() → UploadQueue.enqueue(...)
 *   2. NexusStreamingClient (tick) → UploadQueue.processTick(4)
 *   3. processTick → IncrementalUploader.uploadPartial(...)
 */
public class UploadQueue {

    public record UploadRequest(long chunkKey, byte[] data, int offset) {}

    private static final ConcurrentLinkedQueue<UploadRequest> queue = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger totalEnqueued  = new AtomicInteger(0);
    private static final AtomicInteger totalProcessed = new AtomicInteger(0);

    private static IncrementalUploader uploader;

    public static void init(IncrementalUploader u) {
        uploader = u;
    }

    /** Enfileira um pedido de upload incremental. Thread-safe. */
    public static void enqueue(long chunkKey, byte[] data, int offset) {
        queue.offer(new UploadRequest(chunkKey, data, offset));
        totalEnqueued.incrementAndGet();
    }

    /**
     * Processa ate maxPerTick pedidos na render thread.
     * Chamado uma vez por tick pelo NexusStreamingClient.
     */
    public static void processTick(int maxPerTick) {
        if (uploader == null) return;
        int processed = 0;
        UploadRequest req;
        while (processed < maxPerTick && (req = queue.poll()) != null) {
            try {
                uploader.uploadPartial(req.chunkKey(), req.data(), req.offset());
                totalProcessed.incrementAndGet();
            } catch (Exception e) {
                NexusStreamingClient.LOGGER.warn("[Streaming] Upload falhou para chunk {}: {}", req.chunkKey(), e.getMessage());
            }
            processed++;
        }
    }

    public static int getPending()   { return queue.size(); }
    public static int getEnqueued()  { return totalEnqueued.get(); }
    public static int getProcessed() { return totalProcessed.get(); }
}