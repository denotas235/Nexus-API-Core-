package com.nexus.textures;

import net.minecraft.client.texture.NativeImage;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class ASTCUploadQueue {

    public static class Entry {
        public final Path                cachePath;
        public final int                 width;
        public final int                 height;
        public       ASTCTextureCategory category;
        public final byte[]              pngBytes; // para recompressão THOROUGH com categoria certa

        public Entry(Path cachePath, int width, int height, ASTCTextureCategory category, byte[] pngBytes) {
            this.cachePath = cachePath;
            this.width     = width;
            this.height    = height;
            this.category  = category;
            this.pngBytes  = pngBytes;
        }
    }

    // Chave: identityHashCode da NativeImage → Entry
    private static final ConcurrentHashMap<Integer, Entry> imageQueue = new ConcurrentHashMap<>();

    // Chave: path real da textura → Entry
    private static final ConcurrentHashMap<String, Entry> pathQueue = new ConcurrentHashMap<>();

    // NativeImageMixin enfileira após compressão
    public static void enqueue(
        NativeImage image,
        Path cachePath,
        int width,
        int height,
        ASTCTextureCategory category,
        byte[] pngBytes
    ) {
        if (image == null) return;
        int key = System.identityHashCode(image);
        imageQueue.put(key, new Entry(cachePath, width, height, category, pngBytes));
    }

    // TextureManagerMixin associa path real à entry
    public static void registerPath(String path, NativeImage image) {
        if (path == null) return;

        // Vanilla pré-comprimida — sem NativeImage
        if (image == null) {
            ASTCTextureCategory category = ASTCTextureCategory.fromPath(path);
            pathQueue.put(path, new Entry(null, 0, 0, category, null));
            return;
        }

        int key     = System.identityHashCode(image);
        Entry entry = imageQueue.remove(key);
        if (entry == null) return;

        // Refina categoria com path real
        entry.category = ASTCTextureCategory.fromPath(path);

        // Resubmete recompressão THOROUGH com categoria correcta
        if (entry.pngBytes != null) {
            BackgroundRecompressor.submit(
                entry.pngBytes, entry.width, entry.height, entry.category
            );
        }

        pathQueue.put(path, entry);
    }

    // TextureManagerMixin consome entry pelo path
    public static Entry poll(String path) {
        if (path == null) return null;
        return pathQueue.remove(path);
    }

    public static void clear() {
        imageQueue.clear();
        pathQueue.clear();
    }

    public static int pendingCount() {
        return pathQueue.size();
    }
}
