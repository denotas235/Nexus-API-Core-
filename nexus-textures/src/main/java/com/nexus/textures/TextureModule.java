package com.nexus.textures;

import java.io.InputStream;
import java.util.Scanner;

public class TextureModule {
    public static void load() {
        try {
            ClassLoader cl = TextureModule.class.getClassLoader();
            InputStream is = cl.getResourceAsStream("assets/maliopt/astc_manifest.json");
            if (is == null) {
                TextureMod.LOGGER.warn("[TextureModule] Manifest not found.");
                return;
            }

            String manifest = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
            String[] lines = manifest.split("\n");

            // Contar total primeiro para inicializar o estado de carregamento
            int total = 0;
            for (String line : lines) {
                if (line.contains("\"file\":")) total++;
            }
            ASTCLoadingState.begin(total);

            long start = System.currentTimeMillis();
            int count = 0;
            for (String line : lines) {
                if (line.contains("\"file\":")) {
                    String[] parts = line.split("\"");
                    if (parts.length >= 4) {
                        String astcPath = parts[3];
                        String key = astcPath.replace(".astc", "");
                        String resPath = "assets/maliopt/" + astcPath;
                        InputStream tex = cl.getResourceAsStream(resPath);
                        if (tex != null) {
                            byte[] data = tex.readAllBytes();
                            ASTCTextureRegistry.put(key, data);
                            count++;
                        }
                    }
                    ASTCLoadingState.increment();
                }
            }
            ASTCLoadingState.finish(System.currentTimeMillis() - start);
            TextureMod.LOGGER.info("[TextureModule] {} ASTC textures loaded.", count);
        } catch (Exception e) {
            TextureMod.LOGGER.error("[TextureModule] Load failed: {}", e.getMessage());
        }
    }
}