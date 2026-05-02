package com.nexuapicore.core.fallback;

import com.nexuapicore.core.ExtensionDatabase;
import com.nexuapicore.core.ExtensionDef;
import com.nexuapicore.core.nativelink.NexusNativeLoader;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class ALLExtensionDetector {

    public static List<String> detectExtensions() {
        Set<String> available = new LinkedHashSet<>();

        // ── 1. GL via LTW (fonte original) ──────────────────────────
        try {
            String glLTW = GL11.glGetString(GL11.GL_EXTENSIONS);
            if (glLTW != null && !glLTW.isEmpty()) {
                Collections.addAll(available, glLTW.split(" "));
                System.out.println("[Nexus] Extensões GL detetadas: " + available.size());
            } else {
                System.err.println("[Nexus] Fallback: glGetString(GL_EXTENSIONS) retornou vazio.");
            }
        } catch (Exception e) {
            System.err.println("[Nexus] Fallback GL/LTW erro: " + e.getMessage());
        }

        // ── 2. GL nativo (via .so) ───────────────────────────────────
        if (NexusNativeLoader.loaded) {
            mergeFrom("GL nativo",     NexusNativeLoader.getGLExtensions(),      available);
            mergeFrom("EGL nativo",    NexusNativeLoader.getEGLExtensions(),     available);
            mergeFrom("Audio nativo",  NexusNativeLoader.getAudioExtensions(),   available);
            mergeFrom("Vulkan nativo", NexusNativeLoader.getVulkanExtensions(),  available);
        } else {
            System.err.println("[Nexus] Native lib não carregada — apenas GL/LTW disponível.");
        }

        System.out.println("[Nexus] Extensões combinadas total: " + available.size());

        // ── 3. Scan contra a base de dados ───────────────────────────
        List<ExtensionDef> allKnown = ExtensionDatabase.INSTANCE.getAllExtensions();
        if (allKnown.isEmpty()) {
            System.out.println("[Nexus] Base de dados não carregada. Listagem bruta.");
            return new ArrayList<>(available);
        }

        System.out.println("[Nexus] ===== LISTA RENDERIZADOR (fonte: GL+EGL+Audio+Vulkan) =====");
        for (ExtensionDef def : allKnown) {
            boolean found = available.contains(def.getName());
            System.out.println("[Nexus] [REN][" + (found ? "OK" : "ERRO") + "] " + def.getName());
        }
        System.out.println("[Nexus] ===== FIM LISTA RENDERIZADOR =====");

        return new ArrayList<>(available);
    }

    // ── helper ───────────────────────────────────────────────────────
    private static void mergeFrom(String label, String raw, Set<String> target) {
        if (raw == null || raw.isEmpty()) return;
        int before = target.size();
        Collections.addAll(target, raw.split("[ \n]+"));
        int added = target.size() - before;
        if (added > 0)
            System.out.println("[Nexus] " + label + ": +" + added + " extensões");
    }
}
