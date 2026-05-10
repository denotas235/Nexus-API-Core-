package com.nexus.textures;

import java.io.*;
import java.nio.file.*;

public class ASTCEncoder {

    private static boolean nativeLoaded = false;

    static {
        tryLoadNative();
    }

    // ── JNI nativa ────────────────────────────────────────────────────────────
    // Comprime RGBA8 bruto para ASTC
    // preset: 0 = FASTEST, 4 = THOROUGH
    private static native byte[] nativeCompress(
        byte[] rgbaData,
        int width,
        int height,
        int blockX,
        int blockY,
        int preset,
        boolean srgb,
        boolean hdr
    );

    // ── API pública ───────────────────────────────────────────────────────────

    public static byte[] compress(
        byte[] rgbaData,
        int width,
        int height,
        ASTCTextureCategory category,
        boolean thorough
    ) {
        if (!nativeLoaded) {
            System.err.println("[NexusASTC] libastcenc.so não carregada — fallback PNG");
            return null;
        }

        int preset = thorough ? 4 : 0;

        try {
            return nativeCompress(
                rgbaData,
                width,
                height,
                category.blockX,
                category.blockY,
                preset,
                category.srgb,
                category.hdr
            );
        } catch (Exception e) {
            System.err.println("[NexusASTC] Erro de compressão: " + e.getMessage());
            return null;
        }
    }

    public static boolean isAvailable() {
        return nativeLoaded;
    }

    // ── Carregamento da .so ───────────────────────────────────────────────────

    private static void tryLoadNative() {
        try {
            // Tenta extrair da JAR para temp e carregar
            InputStream soStream = ASTCEncoder.class
                .getResourceAsStream("/natives/arm64-v8a/libastcenc.so");

            if (soStream == null) {
                System.err.println("[NexusASTC] libastcenc.so não encontrada no JAR");
                return;
            }

            Path tempSo = Files.createTempFile("libastcenc_", ".so");
            tempSo.toFile().deleteOnExit();

            try (InputStream in = soStream) {
                Files.copy(in, tempSo, StandardCopyOption.REPLACE_EXISTING);
            }

            System.load(tempSo.toAbsolutePath().toString());
            nativeLoaded = true;
            System.out.println("[NexusASTC] libastcenc.so carregada com sucesso");

        } catch (Exception e) {
            System.err.println("[NexusASTC] Falha ao carregar libastcenc.so: " + e.getMessage());
            nativeLoaded = false;
        }
    }
}
