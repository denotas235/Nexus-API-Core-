package com.nexus.textures;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * TextureModule — carrega todas as texturas ASTC a partir do astc_manifest.json.
 *
 * Estrutura do manifest:
 *   { "block/stone": {"file": "textures_astc/block/stone.astc", "size": 272}, ... }
 *
 * O caminho de recurso correcto e:
 *   assets/maliopt/<entry.file>
 */
public class TextureModule {

    public static void load() {
        long start = System.currentTimeMillis();
        try {
            ClassLoader cl = TextureModule.class.getClassLoader();

            // Manifest dentro do JAR em assets/maliopt/astc_manifest.json
            InputStream is = cl.getResourceAsStream("assets/maliopt/astc_manifest.json");
            if (is == null) {
                TextureMod.LOGGER.warn("[TextureModule] Manifest not found — verifica se o JAR foi construido com os recursos.");
                return;
            }

            JsonObject root = new Gson().fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            int count = 0;
            int missing = 0;

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                String key = entry.getKey();                           // ex: "block/stone"
                JsonObject meta = entry.getValue().getAsJsonObject();
                String filePath = meta.get("file").getAsString();     // ex: "textures_astc/block/stone.astc"

                String resPath = "assets/maliopt/" + filePath;
                InputStream tex = cl.getResourceAsStream(resPath);

                if (tex == null) {
                    missing++;
                    if (missing <= 5) {
                        TextureMod.LOGGER.debug("[TextureModule] Recurso nao encontrado: {}", resPath);
                    }
                    continue;
                }

                byte[] data = tex.readAllBytes();
                ASTCTextureRegistry.put(key, data);
                count++;
            }

            long elapsed = System.currentTimeMillis() - start;
            TextureMod.LOGGER.info("[TextureModule] {} ASTC textures loaded in {}ms ({} missing).",
                count, elapsed, missing);

        } catch (Exception e) {
            TextureMod.LOGGER.error("[TextureModule] Load failed: {}", e.getMessage(), e);
        }
    }
}
