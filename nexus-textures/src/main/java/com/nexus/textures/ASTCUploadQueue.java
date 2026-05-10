package com.nexus.textures;

import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;

import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;

public class ASTCUploadQueue {

    public static class Entry {
        public final AbstractTexture   texture;
        public final Path              cachePath;
        public final int               width;
        public final int               height;
        public       ASTCTextureCategory category;

        public Entry(AbstractTexture texture, Path cachePath, int width, int height, ASTCTextureCategory category) {
            this.texture   = texture;
            this.cachePath = cachePath;
            this.width     = width;
            this.height    = height;
            this.category  = category;
        }
    }

    // Mapa NativeImage → Entry (preenchido pelo NativeImageMixin)
    private static final ConcurrentHashMap<NativeImage, Entry> imageQueue
        = new ConcurrentHashMap<>();

    // Mapa path string → Entry (preenchido pelo TextureManagerMixin)
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
        imageQueue.put(image, new Entry(null, cachePath, width, height, category));
    }

    // TextureManagerMixin refina categoria com path real
    public static void updateCategory(AbstractTexture texture, ASTCTextureCategory category) {
        // Procura entrada sem texture associada e associa
        imageQueue.forEach((image, entry) -> {
            if (entry.texture == null) {
                entry.category = category;
                pathQueue.put(texture.toString(), entry);
            }
        });
    }

    // TextureManagerMixin consome entrada pelo identifier
    public static Entry pollByIdentifier(String path) {
        return pathQueue.remove(path);
    }

    // Limpa filas — chamado no shutdown
    public static void clear() {
        imageQueue.clear();
        pathQueue.clear();
    }

    public static int pendingCount() {
        return pathQueue.size();
    }
}
