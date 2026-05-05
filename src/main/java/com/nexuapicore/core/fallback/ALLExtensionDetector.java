package com.nexuapicore.core.fallback;

import com.nexuapicore.core.ExtensionDatabase;
import com.nexuapicore.core.ExtensionDef;
import com.nexuapicore.core.nativelink.NexusNativeLoader;
import org.lwjgl.opengl.*;

import java.util.*;

/**
 * ALLExtensionDetector 2.0 — PRIME PRO MAX ULTRA
 *
 * Deteta TODAS as extensões do dispositivo através de todas as fontes possíveis.
 * Para cada fonte, mostra:
 *   - Nome da fonte
 *   - Número de extensões encontradas
 *   - Lista completa com ✅ (presente) ou ❌ (ausente) para cada extensão da base de dados
 *
 * Fontes consultadas (por ordem):
 *   1. GL Nativo (libnexus_mali_core.so → glGetString directo no driver)
 *   2. EGL Nativo (libnexus_mali_core.so → eglQueryString)
 *   3. GL via LTW/Wrapper (glGetString via OpenGL)
 *   4. EGL via GL (glGetString com prefixo EGL_ — algumas extensões EGL aparecem aqui)
 *   5. Vulkan Nativo (libnexus_mali_core.so → vkEnumerateInstanceExtensionProperties)
 *   6. Áudio Nativo (libnexus_mali_core.so → AAudio + OpenAL)
 *
 * Cada extensão da base de dados é verificada em cada fonte e o resultado é
 * exibido num relatório completo no log.
 */
public class ALLExtensionDetector {

    // ── Estrutura de dados para tracking ──────────────────────────────
    private static final Map<String, Set<String>> extensionSources = new LinkedHashMap<>();

    public static List<String> detectExtensions() {
        extensionSources.clear();
        Set<String> allAvailable = new LinkedHashSet<>();

        System.out.println("[Nexus] ╔══════════════════════════════════════════════════════════╗");
        System.out.println("[Nexus] ║   DETECTOR INFALÍVEL 2.0 — SHARINGAN APRIMORADO       ║");
        System.out.println("[Nexus] ╚══════════════════════════════════════════════════════════╝");

        // ── FONTE 1: GL Nativa (via .so) ─────────────────────────────
        if (NexusNativeLoader.loaded) {
            String rawGL = NexusNativeLoader.getGLExtensionsSafe();
            if (!rawGL.isEmpty()) {
                Set<String> extSet = new LinkedHashSet<>();
                Collections.addAll(extSet, rawGL.split("[ \n]+"));
                registerSource("GL Nativo (libnexus_mali_core.so)", extSet, allAvailable);
            }
        }

        // ── FONTE 2: EGL Nativa (via .so) ────────────────────────────
        if (NexusNativeLoader.loaded) {
            String rawEGL = NexusNativeLoader.getEGLExtensionsSafe();
            if (!rawEGL.isEmpty()) {
                Set<String> extSet = new LinkedHashSet<>();
                Collections.addAll(extSet, rawEGL.split("[ \n]+"));
                registerSource("EGL Nativo (libnexus_mali_core.so)", extSet, allAvailable);
            }
        }

        // ── FONTE 3: GL via LTW/Wrapper ──────────────────────────────
        try {
            int count = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
            if (count > 0) {
                Set<String> ltwExts = new LinkedHashSet<>();
                for (int i = 0; i < count; i++) {
                    String ext = GL30.glGetStringi(GL11.GL_EXTENSIONS, i);
                    if (ext != null && !ext.isEmpty()) ltwExts.add(ext);
                }
                registerSource("GL via LTW/Wrapper (glGetStringi, " + count + " extensões)", ltwExts, allAvailable);
            }
        } catch (Exception e) {
            // Fallback: string plana
            try {
                String flat = GL11.glGetString(GL11.GL_EXTENSIONS);
                if (flat != null && !flat.isEmpty()) {
                    Set<String> ltwExts = new LinkedHashSet<>();
                    Collections.addAll(ltwExts, flat.split(" "));
                    registerSource("GL via LTW/Wrapper (glGetString)", ltwExts, allAvailable);
                }
            } catch (Exception ex) {
                System.err.println("[Nexus] Fonte GL LTW: " + ex.getMessage());
            }
        }

        // ── FONTE 4: Vulkan ──────────────────────────────────────────
        if (NexusNativeLoader.loaded) {
            String rawVk = NexusNativeLoader.getVulkanExtensionsSafe();
            if (!rawVk.isEmpty()) {
                Set<String> vkSet = new LinkedHashSet<>();
                Collections.addAll(vkSet, rawVk.split("[ \n]+"));
                registerSource("Vulkan (libnexus_mali_core.so)", vkSet, allAvailable);
            }
        }

        // ── FONTE 5: Áudio ───────────────────────────────────────────
        if (NexusNativeLoader.loaded) {
            String rawAudio = NexusNativeLoader.getAudioExtensionsSafe();
            if (!rawAudio.isEmpty()) {
                Set<String> audioSet = new LinkedHashSet<>();
                Collections.addAll(audioSet, rawAudio.split("[ \n]+"));
                registerSource("Áudio Nativo (libnexus_mali_core.so)", audioSet, allAvailable);
            }
        }

        // ── FONTE 6: EGL via sistema ─────────────────────────────────
        try {
            String eglExts = System.getProperty("egl.extensions", "");
            if (!eglExts.isEmpty()) {
                Set<String> eglSys = new LinkedHashSet<>();
                Collections.addAll(eglSys, eglExts.split("[ \n]+"));
                registerSource("EGL Sistema (java property)", eglSys, allAvailable);
            }
        } catch (Exception ignored) {}

        // ── SCAN COMPLETO CONTRA A BASE DE DADOS ─────────────────────
        List<ExtensionDef> allKnown = ExtensionDatabase.INSTANCE.getAllExtensions();
        System.out.println("[Nexus] ");
        System.out.println("[Nexus] ╔══════════════════════════════════════════════════════════╗");
        System.out.println("[Nexus] ║   SCAN COMPLETO — " + allKnown.size() + " extensões conhecidas              ║");
        System.out.println("[Nexus] ╚══════════════════════════════════════════════════════════╝");
        System.out.println("[Nexus] Total combinado de extensões detectadas: " + allAvailable.size());
        System.out.println("[Nexus] ");

        // Agrupar por grupo (GL_ARM, GL_EXT, etc.)
        Map<String, List<ExtensionDef>> byGroup = new LinkedHashMap<>();
        for (ExtensionDef def : allKnown) {
            byGroup.computeIfAbsent(def.getGroup(), k -> new ArrayList<>()).add(def);
        }

        for (Map.Entry<String, List<ExtensionDef>> groupEntry : byGroup.entrySet()) {
            String group = groupEntry.getKey();
            List<ExtensionDef> defs = groupEntry.getValue();
            System.out.println("[Nexus] ── " + group + " (" + defs.size() + " extensões) ──");
            for (ExtensionDef def : defs) {
                boolean found = allAvailable.contains(def.getName());
                String symbol = found ? "✅" : "❌";
                // Encontrar qual fonte forneceu esta extensão
                String source = findSource(def.getName());
                String sourceInfo = found ? "  ← " + source : "";
                System.out.println("[Nexus]   " + symbol + " " + def.getName() + sourceInfo);
            }
            System.out.println();
        }

        System.out.println("[Nexus] ╔══════════════════════════════════════════════════════════╗");
        System.out.println("[Nexus] ║   RESUMO FINAL                                           ║");
        System.out.println("[Nexus] ╚══════════════════════════════════════════════════════════╝");
        int totalOK = 0;
        int totalERR = 0;
        for (ExtensionDef def : allKnown) {
            if (allAvailable.contains(def.getName())) totalOK++; else totalERR++;
        }
        System.out.println("[Nexus]   ✅ Presentes: " + totalOK);
        System.out.println("[Nexus]   ❌ Ausentes : " + totalERR);
        System.out.println("[Nexus]   Total base : " + allKnown.size());
        System.out.println("[Nexus]   Detetadas   : " + allAvailable.size());
        System.out.println("[Nexus] ══════════════════════════════════════════════════════════");

        return new ArrayList<>(allAvailable);
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private static void registerSource(String label, Set<String> exts, Set<String> global) {
        extensionSources.put(label, exts);
        int before = global.size();
        global.addAll(exts);
        int added = global.size() - before;
        System.out.println("[Nexus] 📡 " + label + ": " + exts.size() + " extensões (+" + added + " novas)");
    }

    private static String findSource(String extName) {
        List<String> sources = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : extensionSources.entrySet()) {
            if (entry.getValue().contains(extName)) {
                sources.add(entry.getKey());
            }
        }
        return sources.isEmpty() ? "—" : String.join(" | ", sources);
    }
}
