package com.nexus.modules.textures;

import java.util.concurrent.ConcurrentHashMap;

public class ASTCTextureRegistry {
    private static final ConcurrentHashMap<String, byte[]> astcFiles = new ConcurrentHashMap<>();

    public static void put(String path, byte[] data) {
        astcFiles.put(path, data);
    }

    public static byte[] getASTCData(String path) {
        return astcFiles.get(path);
    }

    public static boolean hasASTCTextures() {
        return !astcFiles.isEmpty();
    }

    public static int count() {
        return astcFiles.size();
    }

    public static void clear() {
        astcFiles.clear();
    }
}
