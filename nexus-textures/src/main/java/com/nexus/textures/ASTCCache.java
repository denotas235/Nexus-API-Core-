package com.nexus.textures;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ASTCCache {
    private static final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    public static byte[] get(String key)              { return cache.get(key); }
    public static void put(String key, byte[] astcData) { cache.put(key, astcData); }
    public static boolean has(String key)             { return cache.containsKey(key); }
    public static int size()                          { return cache.size(); }
    public static void clear()                        { cache.clear(); }
}