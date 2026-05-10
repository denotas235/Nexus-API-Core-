package com.nexus.textures;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class ASTCCache {

    private static final Path CACHE_DIR = Path.of(
        System.getProperty("user.home"), ".minecraft", "astc_cache"
    );

    static {
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar directório ASTC cache", e);
        }
    }

    // Retorna path do ficheiro .astc em cache, ou null se não existir
    public static Path getCached(byte[] pngBytes, ASTCTextureCategory category, boolean thorough) {
        String key  = buildKey(pngBytes, category, thorough);
        Path   path = CACHE_DIR.resolve(key + ".astc");
        return Files.exists(path) ? path : null;
    }

    // Guarda dados ASTC comprimidos no cache
    public static Path save(byte[] pngBytes, ASTCTextureCategory category, boolean thorough, byte[] astcData) {
        String key  = buildKey(pngBytes, category, thorough);
        Path   path = CACHE_DIR.resolve(key + ".astc");
        try {
            Files.write(path, astcData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[NexusASTC] Falha ao guardar cache: " + e.getMessage());
        }
        return path;
    }

    // Lê dados ASTC do cache
    public static byte[] load(Path cachePath) {
        try {
            return Files.readAllBytes(cachePath);
        } catch (IOException e) {
            System.err.println("[NexusASTC] Falha ao ler cache: " + e.getMessage());
            return null;
        }
    }

    // Remove versão FASTEST depois de THOROUGH estar pronta
    public static void evictFastest(byte[] pngBytes, ASTCTextureCategory category) {
        String key  = buildKey(pngBytes, category, false);
        Path   path = CACHE_DIR.resolve(key + ".astc");
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }

    public static boolean hasThorough(byte[] pngBytes, ASTCTextureCategory category) {
        return getCached(pngBytes, category, true) != null;
    }

    // Chave: sha256(png) + categoria + qualidade
    private static String buildKey(byte[] pngBytes, ASTCTextureCategory category, boolean thorough) {
        String hash    = sha256(pngBytes);
        String quality = thorough ? "thorough" : "fastest";
        return hash + "_" + category.name().toLowerCase()
             + "_" + category.blockX + "x" + category.blockY
             + "_" + quality;
    }

    private static String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest    = md.digest(data);
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Path getCacheDir() {
        return CACHE_DIR;
    }
}
