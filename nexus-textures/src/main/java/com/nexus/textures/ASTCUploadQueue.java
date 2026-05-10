package com.nexus.textures;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class ASTCUploadQueue {

    public static class Entry {
        public final Path                cachePath;
        public final int                 width;
        public final int                 height;
        public       ASTCTextureCategory category;

        public Entry(Path cachePath, int width, int height, ASTCTextureCategory category) {
            this.cachePath = cachePath;
            this.width     = width;
            this.height    = height;
            this.category  = category;
        }
    }

    // Chave: identityHashCode da NativeImage → Entry
    private static final ConcurrentHashMap<Integer, Entry> imageQueue
        = new ConcurrentHashMap<>();

    // Chave: path real da textura → Entry
    private static final ConcurrentHashMap<String, Entry> pathQueue
        = new ConcurrentHashMap<>();

    // NativeImageMixin enfileira após compressão
    public static void enqueue(
        NativeImage image,
        Path cachePath,
        int width,
        int height,
        ASTCTextureCategory category
    ) {
        int key = System.identityHashCode(image);
        imageQueue.put(key, new Entry(cachePath, width, height, category));
    }

    // TextureManagerMixin associa path real à entry
    public static void registerPath(String path, NativeImage image) {
        int key   = System.identityHashCode(image);
        Entry entry = imageQueue.remove(key);
        if (entry == null) return;
        entry.category = ASTCTextureCategory.fromPath(path);
        pathQueue.put(path, entry);
    }

    // TextureManagerMixin consome entry pelo path
    public static Entry poll(String path) {
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
