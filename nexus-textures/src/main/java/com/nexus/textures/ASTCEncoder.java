package com.nexus.textures;

import java.io.*;
import java.nio.file.*;

public class ASTCEncoder {

    private static boolean nativeLoaded = false;
    private static Path    astcencBin   = null;

    static {
        tryLoadNative();
    }

    // ── Carregamento do binário astcenc ───────────────────────────────────────

    private static void tryLoadNative() {
        try {
            InputStream soStream = ASTCEncoder.class
                .getResourceAsStream("/natives/arm64-v8a/libastcenc.so");

            if (soStream == null) {
                System.err.println("[NexusASTC] libastcenc.so não encontrada no JAR");
                return;
            }

            Path tempBin = Files.createTempFile("astcenc_", ".bin");
            tempBin.toFile().deleteOnExit();

            try (InputStream in = soStream) {
                Files.copy(in, tempBin, StandardCopyOption.REPLACE_EXISTING);
            }

            // Torna executável
            tempBin.toFile().setExecutable(true);
            astcencBin   = tempBin;
            nativeLoaded = true;
            System.out.println("[NexusASTC] astcenc extraído: " + tempBin);

        } catch (Exception e) {
            System.err.println("[NexusASTC] Falha ao extrair astcenc: " + e.getMessage());
            nativeLoaded = false;
        }
    }

    // ── API pública ───────────────────────────────────────────────────────────

    public static byte[] compress(
        byte[] rgbaData,
        int width,
        int height,
        ASTCTextureCategory category,
        boolean thorough
    ) {
        if (!nativeLoaded || astcencBin == null) {
            System.err.println("[NexusASTC] astcenc não disponível — fallback PNG");
            return null;
        }

        Path tempPng  = null;
        Path tempAstc = null;

        try {
            // Escreve RGBA raw como PNG temporário
            tempPng  = Files.createTempFile("nexus_in_",  ".png");
            tempAstc = Files.createTempFile("nexus_out_", ".astc");
            tempPng.toFile().deleteOnExit();
            tempAstc.toFile().deleteOnExit();

            // Escreve PNG usando raw RGBA
            writePng(rgbaData, width, height, tempPng);

            // Monta comando astcenc
            String block   = category.blockX + "x" + category.blockY;
            String quality = thorough ? "-thorough" : "-fastest";
            String mode    = category.hdr ? "-ch" : (category.srgb ? "-cs" : "-cl");

            ProcessBuilder pb = new ProcessBuilder(
                astcencBin.toAbsolutePath().toString(),
                mode,
                tempPng.toAbsolutePath().toString(),
                tempAstc.toAbsolutePath().toString(),
                block,
                quality
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Lê output para evitar bloqueio
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Silencioso — só loga erros
                    if (line.toLowerCase().contains("error")) {
                        System.err.println("[NexusASTC] astcenc: " + line);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("[NexusASTC] astcenc falhou com código: " + exitCode);
                return null;
            }

            // Lê ficheiro ASTC resultante
            byte[] result = Files.readAllBytes(tempAstc);
            if (result.length < 16) {
                System.err.println("[NexusASTC] ASTC output inválido");
                return null;
            }

            return result;

        } catch (Exception e) {
            System.err.println("[NexusASTC] Erro de compressão: " + e.getMessage());
            return null;
        } finally {
            // Limpa temporários
            tryDelete(tempPng);
            tryDelete(tempAstc);
        }
    }

    // Escreve PNG mínimo a partir de RGBA8 raw
    private static void writePng(byte[] rgba, int width, int height, Path out) throws Exception {
        // Usa javax.imageio para escrever PNG correctamente
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int r = rgba[i]     & 0xFF;
                int g = rgba[i + 1] & 0xFF;
                int b = rgba[i + 2] & 0xFF;
                int a = rgba[i + 3] & 0xFF;
                img.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        javax.imageio.ImageIO.write(img, "PNG", out.toFile());
    }

    private static void tryDelete(Path p) {
        try { if (p != null) Files.deleteIfExists(p); }
        catch (Exception ignored) {}
    }

    public static boolean isAvailable() { return nativeLoaded; }
}
