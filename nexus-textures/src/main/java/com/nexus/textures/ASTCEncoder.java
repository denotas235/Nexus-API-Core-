package com.nexus.textures;

import java.io.*;
import java.nio.file.*;
import org.lwjgl.stb.STBImageWrite;

public class ASTCEncoder {

    private static boolean nativeLoaded = false;
    private static Path    astcencBin   = null;
    private static String  loadError    = null;

    static {
        tryLoadNative();
    }

    private static void tryLoadNative() {
        String resPath = "/natives/arm64-v8a/libastcenc.so";
        try {
            InputStream soStream = ASTCEncoder.class.getResourceAsStream(resPath);
            if (soStream == null) {
                loadError = "libastcenc.so não encontrada no JAR em: " + resPath
                        + "  — resource pack externo não terá compressão ASTC em runtime.";
                System.err.println("[NexusASTC] " + loadError);
                return;
            }
            Path tempBin = Files.createTempFile("astcenc_", ".bin");
            tempBin.toFile().deleteOnExit();
            try (InputStream in = soStream) {
                Files.copy(in, tempBin, StandardCopyOption.REPLACE_EXISTING);
            }
            tempBin.toFile().setExecutable(true);
            astcencBin   = tempBin;
            nativeLoaded = true;
            System.out.println("[NexusASTC] astcenc-neon (ARM64) extraído com sucesso: "
                    + tempBin + "  (" + Files.size(tempBin) / 1024 + " KB)"
                    + " — compressão ASTC em runtime ATIVA.");
        } catch (Exception e) {
            loadError = "Falha ao extrair astcenc: " + e.getMessage();
            System.err.println("[NexusASTC] " + loadError);
            nativeLoaded = false;
        }
    }

    public static byte[] compress(
            byte[] rgbaData,
            int width,
            int height,
            ASTCTextureCategory category,
            boolean thorough
    ) {
        if (!nativeLoaded || astcencBin == null) {
            System.err.println("[NexusASTC] astcenc não disponível — fallback PNG. Razão: " + loadError);
            return null;
        }
        Path tempPng  = null;
        Path tempAstc = null;
        try {
            tempPng  = Files.createTempFile("nexus_in_",  ".png");
            tempAstc = Files.createTempFile("nexus_out_", ".astc");
            tempPng.toFile().deleteOnExit();
            tempAstc.toFile().deleteOnExit();

            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(width * height * 4);
            buf.put(rgbaData);
            buf.flip();
            STBImageWrite.stbi_write_png(tempPng.toAbsolutePath().toString(), width, height, 4, buf, width * 4);

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
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
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
            tryDelete(tempPng);
            tryDelete(tempAstc);
        }
    }

    private static void tryDelete(Path p) {
        try { if (p != null) Files.deleteIfExists(p); }
        catch (Exception ignored) {}
    }

    public static boolean isAvailable() { return nativeLoaded; }
    public static String getLoadError()  { return loadError; }
}