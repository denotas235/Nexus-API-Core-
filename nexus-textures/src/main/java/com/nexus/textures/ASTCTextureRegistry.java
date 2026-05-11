package com.nexus.textures;

import java.util.concurrent.ConcurrentHashMap;

public class ASTCTextureRegistry {
    private static final ConcurrentHashMap<String, byte[]> store = new ConcurrentHashMap<>();

    public static void put(String key, byte[] data) { store.put(key, data); }
    public static byte[] get(String key) { return store.get(key); }
    public static boolean has(String key) { return store.containsKey(key); }
    public static int count() { return store.size(); }
}
