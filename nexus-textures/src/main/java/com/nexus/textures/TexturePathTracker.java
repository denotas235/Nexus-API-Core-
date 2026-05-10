package com.nexus.textures;

public class TexturePathTracker {
    private static final ThreadLocal<String> currentTexturePath = new ThreadLocal<>();

    public static void setCurrentPath(String path) { currentTexturePath.set(path); }
    public static String getCurrentPath() { return currentTexturePath.get(); }
    public static void clear() { currentTexturePath.remove(); }
}
