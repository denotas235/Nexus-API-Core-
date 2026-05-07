package com.nexus.modules.textures;

import com.maliopt.MaliOptMod;
import java.io.InputStream;
import java.util.Scanner;

public class TextureModule {
    public static void load() {
        try {
            ClassLoader cl = TextureModule.class.getClassLoader();
            InputStream manifestStream = cl.getResourceAsStream("assets/maliopt/astc_manifest.json");
            if (manifestStream == null) {
                MaliOptMod.LOGGER.warn("[TextureModule] Manifesto ASTC não encontrado no classpath.");
                return;
            }
            String manifest = new Scanner(manifestStream, "UTF-8").useDelimiter("\\A").next();
            String[] lines = manifest.split("\\n");
            int count = 0;
            for (String line : lines) {
                if (line.contains("\"file\":")) {
                    String[] parts = line.split("\"");
                    if (parts.length >= 4) {
                        String astcPath = parts[3]; // ex: "textures_astc/block/stone.astc"
                        String key = astcPath.replace(".astc", "");
                        String resourcePath = "assets/maliopt/" + astcPath;
                        InputStream input = cl.getResourceAsStream(resourcePath);
                        if (input != null) {
                            byte[] bytes = input.readAllBytes();
                            ASTCTextureRegistry.put(key, bytes);
                            count++;
                        }
                    }
                }
            }
            MaliOptMod.LOGGER.info("[TextureModule] {} texturas ASTC carregadas do JAR.", count);
        } catch (Exception e) {
            MaliOptMod.LOGGER.warn("[TextureModule] Erro ao carregar texturas ASTC: {}", e.getMessage());
        }
    }
}
