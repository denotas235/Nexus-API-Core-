package com.nexuapicore.core.fallback;

import com.nexuapicore.core.ExtensionDatabase;
import com.nexuapicore.core.ExtensionDef;
import org.lwjgl.opengl.GL11;

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
                return available;
            }

            available = new ArrayList<>(Arrays.asList(extensionsString.split(" ")));
            System.out.println("[Nexus] Extensões GL detetadas: " + available.size());

            List<ExtensionDef> allKnown = ExtensionDatabase.INSTANCE.getAllExtensions();
            if (allKnown.isEmpty()) {
                System.out.println("[Nexus] Base de dados não carregada. Listagem bruta.");
                return available;
            }

            System.out.println("[Nexus] ===== LISTA RENDERIZADOR (fonte: GL/LTW) =====");
            for (ExtensionDef def : allKnown) {
                boolean found = available.contains(def.getName());
                String status = found ? "OK" : "ERRO";
                System.out.println("[Nexus] [REN][" + status + "] " + def.getName());
            }
            System.out.println("[Nexus] ===== FIM LISTA RENDERIZADOR =====");

        } catch (Exception e) {
            System.err.println("[Nexus] Fallback: erro: " + e.getMessage());
            e.printStackTrace();
        }

        return available;
    }
}
