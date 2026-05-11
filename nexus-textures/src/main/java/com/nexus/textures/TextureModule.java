package com.nexus.textures;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * TextureModule — carrega texturas ASTC pre-comprimidas do astc_manifest.json.
 *
 * Estrutura do manifest:
 *   { "block/stone": {"file": "textures_astc/block/stone.astc", "size": 272}, ... }
 *
 * Fix: a versao anterior usava parsing por linha com split(\"\\\"\") e derivava
 * a chave do path (ex: "textures_astc/block/stone" em vez de "block/stone"),
 * o que causava que os lookups em ASTCTextureRegistry nunca encontrassem nada.
 *
 * Agora usa Gson para parsing correcto e usa o JSON key como chave de registo.
 */
public class TextureModule {

    public static void load() {
        long start = System.currentTimeMillis();
        try {
            ClassLoader cl = TextureModule.class.getClassLoader();
            InputStream is = cl.getResourceAsStream("assets/maliopt/astc_manifest.json");
            if (is == null) {
                TextureMod.LOGGER.warn("[TextureModule] Manifest nao encontrado — verifica se o JAR foi construido com os recursos.");
                return;
            }

            JsonObject root = new Gson().fromJson(
                new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);

            int total = root.size();
            ASTCLoadingState.begin(total);

            int count   = 0;
            int missing = 0;

            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                // Chave correcta: "block/stone" (nao derivada do path)
                String key      = entry.getKey();
                JsonObject meta = entry.getValue().getAsJsonObject();
                String filePath = meta.get("file").getAsString();

                String resPath = "assets/maliopt/" + filePath;
                InputStream tex = cl.getResourceAsStream(resPath);

                if (tex == null) {
                    missing++;
                    if (missing <= 3) {
                        TextureMod.LOGGER.debug("[TextureModule] Recurso nao encontrado: {}", resPath);
                    }
                    ASTCLoadingState.increment();
                    continue;
                }

                byte[] data = tex.readAllBytes();
                ASTCTextureRegistry.put(key, data);
                count++;
                ASTCLoadingState.increment();
            }

            long elapsed = System.currentTimeMillis() - start;
            ASTCLoadingState.finish(elapsed);
            TextureMod.LOGGER.info("[TextureModule] {} ASTC textures loaded in {}ms ({} missing).",
                count, elapsed, missing);

        } catch (Exception e) {
            TextureMod.LOGGER.error("[TextureModule] Load failed: {}", e.getMessage(), e);
        }
    }
}

