package com.nexus.modules.textures;

import com.maliopt.MaliOptMod;
import java.io.InputStream;

public class TextureModule {
    public static void load() {
        try {
            ClassLoader cl = TextureModule.class.getClassLoader();
            InputStream manifestStream = cl.getResourceAsStream("assets/maliopt/astc_manifest.json");
            if (manifestStream != null) {
                String manifest = new String(manifestStream.readAllBytes());
                String[] lines = manifest.split("\\n");
                for (String line : lines) {
                    if (line.contains("\"file\":")) {
                        String[] parts = line.split("\"");
                        if (parts.length >= 4) {
                            String astcPath = parts[3]; // ex: "textures_astc/block/stone.astc"
                            String resourcePath = "assets/maliopt/" + astcPath;
                            InputStream input = cl.getResourceAsStream(resourcePath);
                            if (input != null) {
                                byte[] bytes = input.readAllBytes();
                                String key = astcPath.replace(".astc", "");
                                ASTCTextureRegistry.put(key, bytes);
                            }
                        }
                    }
                }
                MaliOptMod.LOGGER.info("[TextureModule] {} texturas ASTC carregadas", ASTCTextureRegistry.count());
            }
        } catch (Exception e) {
            MaliOptMod.LOGGER.warn("[TextureModule] Erro ao carregar texturas ASTC: {}", e.getMessage());
        }
    }
}
