package com.maliopt.astc;

import com.maliopt.MaliOptMod;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;

/**
 * ASTCCacheManager — guarda e carrega texturas ASTC comprimidas
 * Estrutura: .minecraft/nexus-astc-cache/{hash}.astc
 */
public final class ASTCCacheManager {

    private static Path cacheDir;
    private static boolean ready = false;

    private ASTCCacheManager() {}
    public static boolean isReady() { return ready; }

    public static void init() {
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            cacheDir = gameDir.resolve("nexus-astc-cache");
            Files.createDirectories(cacheDir);
            ready = true;
            MaliOptMod.LOGGER.info("[ASTCCache] Cache inicializada em {}", cacheDir);
        } catch (IOException e) {
            MaliOptMod.LOGGER.warn("[ASTCCache] Falha ao criar diretório: {}", e.getMessage());
            ready = false;
        }
    }

    public static boolean exists(String hash, int w, int h, int bx, int by) {
        if (!ready) return false;
        String fileName = hash + "_" + w + "x" + h + "_" + bx + "x" + by + ".astc";
        return Files.exists(cacheDir.resolve(fileName));
    }

    public static byte[] load(String hash, int w, int h, int bx, int by) {
        if (!ready) return null;
        String fileName = hash + "_" + w + "x" + h + "_" + bx + "x" + by + ".astc";
        try {
            return Files.readAllBytes(cacheDir.resolve(fileName));
        } catch (IOException e) {
            return null;
        }
    }

    public static void save(String hash, int w, int h, int bx, int by, byte[] data) {
        if (!ready) return;
        String fileName = hash + "_" + w + "x" + h + "_" + bx + "x" + by + ".astc";
        try {
            Files.write(cacheDir.resolve(fileName), data);
            MaliOptMod.LOGGER.debug("[ASTCCache] Guardado: {} ({} bytes)", fileName, data.length);
        } catch (IOException e) {
            MaliOptMod.LOGGER.warn("[ASTCCache] Erro ao guardar: {}", e.getMessage());
        }
    }

    public static String hashImage(byte[] rgba, int w, int h) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(rgba, 0, Math.min(rgba.length, 4096));
            md.update((byte)(w >> 24)); md.update((byte)(w >> 16));
            md.update((byte)(w >> 8));  md.update((byte)w);
            md.update((byte)(h >> 24)); md.update((byte)(h >> 16));
            md.update((byte)(h >> 8));  md.update((byte)h);
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString().substring(0, 16);
        } catch (Exception e) {
            return Integer.toHexString(java.util.Arrays.hashCode(rgba));
        }
    }
}
