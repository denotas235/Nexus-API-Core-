package com.nexus.streaming;

import java.util.List;

public class StreamingDebugHud {
    public static void addLeftLines(List<String> lines) {
        lines.add("");
        boolean ready = StreamingPipeline.isInitialized();
        lines.add("[Nexus Streaming] " + (ready ? "ON" : "A inicializar..."));
        if (ready) {
            lines.add("  BufferStorage:  " + (StreamingPipeline.hasBufferStorage()  ? "ON \u2714 (zero-copia)" : "OFF \u2718 (glBufferSubData)"));
            lines.add("  MapBufferRange: " + (StreamingPipeline.hasMapBufferRange() ? "ON \u2714" : "OFF \u2718"));
            lines.add("  UnpackSubimage: " + (StreamingPipeline.hasUnpackSubimage() ? "ON \u2714" : "OFF \u2718"));
            lines.add("  Fila uploads:   pendentes=" + UploadQueue.getPending()
                    + "  total=" + UploadQueue.getProcessed());
        }
    }
}