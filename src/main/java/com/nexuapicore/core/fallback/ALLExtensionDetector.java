package com.nexuapicore.core.fallback;

import com.nexuapicore.core.ExtensionDatabase;
import com.nexuapicore.core.ExtensionDef;
import org.lwjgl.opengl.GL11;  // disponível no ambiente Minecraft

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ALLExtensionDetector {

    public static List<String> detectExtensions() {
        System.out.println("[Nexus] Fallback: detetando extensões via GL...");

        List<String> available = new ArrayList<>();

        try {
            String extensionsString = GL11.glGetString(GL11.GL_EXTENSIONS);
            if (extensionsString == null || extensionsString.isEmpty()) {
                System.err.println("[Nexus] Fallback: glGetString(GL_EXTENSIONS) retornou vazio.");
                return available; // lista vazia
            }

            available = Arrays.asList(extensionsString.split(" "));
            System.out.println("[Nexus] Extensões GL detetadas: " + available.size());

            // Comparar com a base de dados das 173 extensões
            List<ExtensionDef> allKnown = ExtensionDatabase.getAllExtensions();
            if (allKnown.isEmpty()) {
                System.out.println("[Nexus] Base de dados de extensões não carregada. Apenas listagem bruta.");
                for (String ext : available) {
                    System.out.println("[Nexus] Extensão detetada (raw): " + ext);
                }
                return available;
            }

            // Log detalhado (OK / ERRO) para cada extensão da nossa lista
            System.out.println("[Nexus] ===== VERIFICAÇÃO DAS 173 EXTENSÕES =====");
            for (ExtensionDef def : allKnown) {
                boolean found = available.contains(def.name);
                String status = found ? "OK" : "ERRO";
                System.out.println("[Nexus] [" + status + "] " + def.name);
            }
            System.out.println("[Nexus] ===== FIM DA VERIFICAÇÃO =====");

        } catch (Exception e) {
            System.err.println("[Nexus] Fallback: erro ao detetar extensões: " + e.getMessage());
            e.printStackTrace();
        }

        return available;
    }
}
