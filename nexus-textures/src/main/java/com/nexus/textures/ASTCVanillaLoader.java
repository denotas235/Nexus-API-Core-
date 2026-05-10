package com.nexus.textures;

import java.io.InputStream;
import java.nio.file.*;

public class ASTCVanillaLoader {

    private static final String ASTC_PREFIX = "/assets/nexus-textures/astc_cache/";

    // Verifica se existe versão pré-comprimida desta textura no JAR
    public static byte[] loadPrecompressed(String texturePath) {
        if (texturePath == null) return null;

        try {
            // Converte path: assets/minecraft/textures/block/stone.png
            //            → /assets/nexus-textures/astc_cache/assets/minecraft/textures/block/stone.astc
            String astcPath = ASTC_PREFIX + texturePath.replace(".png", ".astc");

            InputStream stream = ASTCVanillaLoader.class.getResourceAsStream(astcPath);
            if (stream == null) return null;

            byte[] data = stream.readAllBytes();
            stream.close();

            if (data.length < 16) return null;

            System.out.println("[NexusASTC] Vanilla pré-comprimida: " + texturePath);
            return data;

        } catch (Exception e) {
            return null;
        }
    }

    public static boolean hasPrecompressed(String texturePath) {
        if (texturePath == null) return false;
        String astcPath = ASTC_PREFIX + texturePath.replace(".png", ".astc");
        return ASTCVanillaLoader.class.getResourceAsStream(astcPath) != null;
    }
}
