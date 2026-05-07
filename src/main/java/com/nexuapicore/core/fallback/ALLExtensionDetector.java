package com.nexuapicore.core.fallback;

import com.nexuapicore.core.ExtensionDatabase;
import com.nexuapicore.core.ExtensionDef;
import com.nexuapicore.core.nativelink.NexusNativeLoader;
import org.lwjgl.opengl.*;

import java.io.*;
import java.util.*;

/**
 * ALLExtensionDetector 3.0 — SHARINGAN RINNEGAN
 * Lê extensões diretamente das bibliotecas do sistema, sem depender de cópias.
 */
public class ALLExtensionDetector {

    // Caminhos reais das bibliotecas no sistema Android
    private static final String[] SYSTEM_GRAPHICS_LIBS = {
        "/vendor/lib64/egl/libEGL.so",
        "/vendor/lib64/egl/libGLESv1_CM.so",
        "/vendor/lib64/egl/libGLESv2.so",
        "/vendor/lib64/egl/libGLESv3.so",
        "/vendor/lib64/egl/libGLES_mali.so",
        "/vendor/lib64/egl/libGLES_meow.so",
        "/vendor/lib64/hw/vulkan.mt6768.so",
        "/system/lib64/libvulkan.so",
        "/vendor/lib64/libmobileglues.so",
        "/vendor/lib64/egl/libmobileglues_info_getter.so"
    };

    private static final Map<String, Set<String>> extensionSources = new LinkedHashMap<>();

    public static List<String> detectExtensions() {
        extensionSources.clear();
        Set<String> allAvailable = new LinkedHashSet<>();

        log("═══ DETECTOR INFALÍVEL 3.0 ═══");

        // FONTE 1: GL Nativa
        if (NexusNativeLoader.loaded) {
            String rawGL = NexusNativeLoader.getGLExtensionsSafe();
            if (!rawGL.isEmpty()) {
                Set<String> extSet = new LinkedHashSet<>();
                Collections.addAll(extSet, rawGL.split("[ \n]+"));
                registerSource("GL Nativo", extSet, allAvailable);
            }
        }

        // FONTE 2: EGL Nativa
        if (NexusNativeLoader.loaded) {
            String rawEGL = NexusNativeLoader.getEGLExtensionsSafe();
            if (!rawEGL.isEmpty()) {
                Set<String> extSet = new LinkedHashSet<>();
                Collections.addAll(extSet, rawEGL.split("[ \n]+"));
                registerSource("EGL Nativo", extSet, allAvailable);
            }
        }

        // FONTE 3: GL via LTW/Wrapper
        try {
            int count = GL11.glGetInteger(GL30.GL_NUM_EXTENSIONS);
            if (count > 0) {
                Set<String> ltwExts = new LinkedHashSet<>();
                for (int i = 0; i < count; i++) {
                    String ext = GL30.glGetStringi(GL11.GL_EXTENSIONS, i);
                    if (ext != null && !ext.isEmpty()) ltwExts.add(ext);
                }
                registerSource("GL (glGetStringi)", ltwExts, allAvailable);
            }
        } catch (Exception e) {
            try {
                String flat = GL11.glGetString(GL11.GL_EXTENSIONS);
                if (flat != null && !flat.isEmpty()) {
                    Set<String> ltwExts = new LinkedHashSet<>();
                    Collections.addAll(ltwExts, flat.split(" "));
                    registerSource("GL (glGetString)", ltwExts, allAvailable);
                }
            } catch (Exception ignored) {}
        }

        // FONTE 4: Vulkan Nativo
        if (NexusNativeLoader.loaded) {
            String rawVk = NexusNativeLoader.getVulkanExtensionsSafe();
            if (!rawVk.isEmpty()) {
                Set<String> vkSet = new LinkedHashSet<>();
                Collections.addAll(vkSet, rawVk.split("[ \n]+"));
                registerSource("Vulkan Nativo", vkSet, allAvailable);
            }
        }

        // FONTE 5: Strings das bibliotecas do sistema (consulta direta)
        Set<String> systemExts = loadSystemGraphicsExtensions();
        if (!systemExts.isEmpty()) {
            registerSource("Sistema (" + countFoundLibs() + " libs)", systemExts, allAvailable);
        }

        // SCAN COMPLETO
        List<ExtensionDef> allKnown = ExtensionDatabase.INSTANCE.getAllExtensions();
        log("Base: " + allKnown.size() + " | Detectadas: " + allAvailable.size());
        
        int present = 0, absent = 0;
        for (ExtensionDef def : allKnown) {
            if (allAvailable.contains(def.getName())) present++; else absent++;
        }
        log("✅ " + present + "  ❌ " + absent);

        return new ArrayList<>(allAvailable);
    }

    private static int countFoundLibs() {
        int found = 0;
        for (String path : SYSTEM_GRAPHICS_LIBS) {
            if (new File(path).exists()) found++;
        }
        return found;
    }

    private static Set<String> loadSystemGraphicsExtensions() {
        Set<String> exts = new LinkedHashSet<>();
        for (String libPath : SYSTEM_GRAPHICS_LIBS) {
            File libFile = new File(libPath);
            if (!libFile.exists()) continue;
            try {
                Process p = new ProcessBuilder("strings", libPath).start();
                Scanner scanner = new Scanner(p.getInputStream()).useDelimiter("\\A");
                if (scanner.hasNext()) {
                    for (String line : scanner.next().split("\\n")) {
                        line = line.trim();
                        if (line.matches("^(GL_|EGL_|VK_)[A-Za-z0-9_]+$")) {
                            exts.add(line);
                        }
                    }
                }
                p.waitFor();
            } catch (Exception ignored) {}
        }
        return exts;
    }

    private static void registerSource(String label, Set<String> exts, Set<String> global) {
        extensionSources.put(label, exts);
        int before = global.size();
        global.addAll(exts);
        log("📡 " + label + ": " + exts.size() + " (+" + (global.size() - before) + ")");
    }

    private static void log(String msg) {
        System.out.println("[Nexus] " + msg);
    }
}
