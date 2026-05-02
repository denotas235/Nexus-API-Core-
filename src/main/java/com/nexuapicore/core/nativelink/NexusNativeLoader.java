package com.nexuapicore.core.nativelink;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NexusNativeLoader — carrega a lib nativa do Nexus Mali Core
 * e deteta libs companheiras do ZalithLauncher2 quando disponíveis.
 *
 * Anti-crash garantido:
 *  - Nunca lança exceção para fora
 *  - Double-load protegido com AtomicBoolean
 *  - Cada fase tem try/catch isolado
 *  - Flags indicam o que está disponível
 *
 * Compatibilidade:
 *  - ZalithLauncher 1.x  → usa só libnexus_mali_core.so
 *  - ZalithLauncher2     → deteta e usa libs adicionais
 *  - Qualquer outro launcher → fallback silencioso
 */
public class NexusNativeLoader {

    // ── Estado ────────────────────────────────────────────────────────────────

    /** true se libnexus_mali_core.so foi carregada com sucesso */
    public static volatile boolean loaded = false;

    /** true se libng_gl4es.so do ZalithLauncher2 foi carregada */
    public static volatile boolean ngGl4esLoaded = false;

    /** true se libEGL_angle.so do ZalithLauncher2 foi carregada */
    public static volatile boolean angleLoaded = false;

    /** true se libvulkan_freedreno.so foi carregada */
    public static volatile boolean freedrenoLoaded = false;

    /** true se libspirv-cross-c-shared.so foi carregada */
    public static volatile boolean spirvCrossLoaded = false;

    /** Path da libnexus_mali_core.so extraída (para debug) */
    public static volatile String nativeCoreLibPath = null;

    /** Guard contra double-load em ambientes multi-thread */
    private static final AtomicBoolean loadStarted = new AtomicBoolean(false);

    // ── Libs companheiras do ZalithLauncher2 a tentar carregar ────────────────
    private static final String[] ZALITH_COMPANION_LIBS = {
        "libng_gl4es.so",
        "libEGL_angle.so",
        "libvulkan_freedreno.so",
        "libspirv-cross-c-shared.so"
    };

    // ── API pública ───────────────────────────────────────────────────────────

    /**
     * Ponto de entrada principal. Seguro para chamar múltiplas vezes.
     * Nunca lança exceção.
     */
    public static void load() {
        // Garante que só uma thread executa o carregamento
        if (!loadStarted.compareAndSet(false, true)) {
            return;
        }

        System.out.println("[Nexus] ===== INICIO CARREGAMENTO NATIVO =====");

        // Fase 1 — Libs companheiras do launcher (não bloqueante)
        loadZalithCompanionLibs();

        // Fase 2 — Lib principal do Nexus
        loadNexusCoreLib();

        // Fase 3 — Inicialização do core nativo
        if (loaded) {
            initCore();
        }

        System.out.println("[Nexus] ===== FIM CARREGAMENTO NATIVO =====");
        printStatus();
    }

    // ── Fase 1: Libs companheiras ─────────────────────────────────────────────

    private static void loadZalithCompanionLibs() {
        String nativeLibPath = "";
        try {
            nativeLibPath = System.getProperty("java.library.path", "");
        } catch (Throwable t) {
            System.err.println("[Nexus] Não foi possível ler java.library.path: " + t.getMessage());
            return;
        }

        if (nativeLibPath.isEmpty()) {
            System.out.println("[Nexus] java.library.path vazio — provavelmente não é ZalithLauncher2");
            return;
        }

        String[] dirs = nativeLibPath.split(":");

        for (String libName : ZALITH_COMPANION_LIBS) {
            tryLoadCompanionLib(libName, dirs);
        }
    }

    private static void tryLoadCompanionLib(String libName, String[] dirs) {
        for (String dir : dirs) {
            if (dir == null || dir.isEmpty()) continue;

            File f = null;
            try {
                f = new File(dir, libName);
            } catch (Throwable t) {
                continue; // path inválido, tenta próximo
            }

            boolean exists = false;
            try {
                exists = f.exists();
            } catch (SecurityException se) {
                continue; // sem permissão para verificar, tenta próximo
            }

            if (!exists) continue;

            try {
                System.load(f.getAbsolutePath());
                markCompanionLoaded(libName, f.getAbsolutePath());
                return; // sucesso — não tenta outros dirs
            } catch (UnsatisfiedLinkError ule) {
                System.err.println("[Nexus] Companion link error (" + libName + "): " + ule.getMessage());
                return; // encontrou mas falhou — não tenta duplicar
            } catch (SecurityException se) {
                System.err.println("[Nexus] Companion sem permissão (" + libName + "): " + se.getMessage());
                return;
            } catch (Throwable t) {
                System.err.println("[Nexus] Companion erro inesperado (" + libName + "): " + t.getMessage());
                return;
            }
        }
        // Não encontrou em nenhum dir — silencioso (launcher sem esta lib)
    }

    private static void markCompanionLoaded(String libName, String path) {
        System.out.println("[Nexus] Companion carregada: " + libName + " ← " + path);
        switch (libName) {
            case "libng_gl4es.so":              ngGl4esLoaded    = true; break;
            case "libEGL_angle.so":             angleLoaded      = true; break;
            case "libvulkan_freedreno.so":      freedrenoLoaded  = true; break;
            case "libspirv-cross-c-shared.so":  spirvCrossLoaded = true; break;
        }
    }

    // ── Fase 2: Lib principal ─────────────────────────────────────────────────

    private static void loadNexusCoreLib() {
        try {
            String arch = detectArch();
            String libName = "libnexus_mali_core.so";
            String resourcePath = "/natives/" + arch + "/" + libName;

            InputStream in = null;
            try {
                in = NexusNativeLoader.class.getResourceAsStream(resourcePath);
            } catch (Throwable t) {
                System.err.println("[Nexus] Erro ao aceder recurso " + resourcePath + ": " + t.getMessage());
                return;
            }

            if (in == null) {
                System.err.println("[Nexus] Recurso não encontrado: " + resourcePath);
                System.err.println("[Nexus] Arch detetada: " + arch);
                return;
            }

            Path tmpDir = null;
            try {
                tmpDir = Files.createTempDirectory("nexus_natives");
            } catch (IOException ioe) {
                System.err.println("[Nexus] Não foi possível criar dir temporário: " + ioe.getMessage());
                closeQuietly(in);
                return;
            }

            Path tmpLib = tmpDir.resolve(libName);
            try {
                Files.copy(in, tmpLib, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ioe) {
                System.err.println("[Nexus] Não foi possível copiar lib: " + ioe.getMessage());
                closeQuietly(in);
                return;
            } finally {
                closeQuietly(in);
            }

            try {
                System.load(tmpLib.toAbsolutePath().toString());
                loaded = true;
                nativeCoreLibPath = tmpLib.toAbsolutePath().toString();
                System.out.println("[Nexus] Core library loaded: " + nativeCoreLibPath);
            } catch (UnsatisfiedLinkError ule) {
                System.err.println("[Nexus] Core link error: " + ule.getMessage());
            } catch (SecurityException se) {
                System.err.println("[Nexus] Core sem permissão: " + se.getMessage());
            }

        } catch (Throwable t) {
            // catch-all para qualquer erro não previsto
            System.err.println("[Nexus] Erro crítico ao carregar core lib: " + t.getMessage());
            if (t instanceof Error) {
                // OutOfMemoryError, etc — reporta mas não relança
                t.printStackTrace();
            }
        }
    }

    private static String detectArch() {
        try {
            String osArch = System.getProperty("os.arch", "");
            // arm64-v8a ou armeabi-v7a
            if (osArch.contains("64") || osArch.equals("aarch64")) {
                return "arm64-v8a";
            } else if (osArch.contains("arm") || osArch.contains("v7")) {
                return "armeabi-v7a";
            } else {
                // fallback — tenta arm64
                System.out.println("[Nexus] Arch desconhecida (" + osArch + "), assumindo arm64-v8a");
                return "arm64-v8a";
            }
        } catch (Throwable t) {
            return "arm64-v8a"; // default seguro
        }
    }

    // ── Fase 3: Init do core ──────────────────────────────────────────────────

    private static void initCore() {
        try {
            boolean ok = initNexusCore();
            if (ok) {
                System.out.println("[Nexus] Nexus Mali Core initialized.");
            } else {
                System.err.println("[Nexus] Nexus Mali Core init failed (initNexusCore retornou false).");
                // Não marca loaded=false — a lib está carregada, só o init falhou
                // As chamadas de extensões ainda podem funcionar parcialmente
            }
        } catch (UnsatisfiedLinkError ule) {
            // initNexusCore() não está no .so — versão antiga da lib
            System.err.println("[Nexus] initNexusCore() não encontrado na lib: " + ule.getMessage());
        } catch (Throwable t) {
            System.err.println("[Nexus] Erro durante initNexusCore(): " + t.getMessage());
        }
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private static void closeQuietly(Closeable c) {
        if (c == null) return;
        try { c.close(); } catch (Throwable ignored) {}
    }

    private static void printStatus() {
        System.out.println("[Nexus] Status do carregamento:");
        System.out.println("[Nexus]   Core (libnexus_mali_core): " + (loaded        ? "✓" : "✗"));
        System.out.println("[Nexus]   libng_gl4es (Zalith2):     " + (ngGl4esLoaded ? "✓" : "✗"));
        System.out.println("[Nexus]   libEGL_angle (Zalith2):    " + (angleLoaded   ? "✓" : "✗"));
        System.out.println("[Nexus]   libvulkan_freedreno:       " + (freedrenoLoaded ? "✓" : "✗"));
        System.out.println("[Nexus]   libspirv-cross:            " + (spirvCrossLoaded ? "✓" : "✗"));
    }

    // ── Chamadas nativas — todas protegidas ───────────────────────────────────

    /**
     * Retorna extensões GL do driver nativo.
     * Retorna string vazia em caso de erro.
     */
    public static String getGLExtensionsSafe() {
        if (!loaded) return "";
        try {
            String result = getGLExtensions();
            return result != null ? result : "";
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("[Nexus] getGLExtensions() não disponível: " + ule.getMessage());
            return "";
        } catch (Throwable t) {
            System.err.println("[Nexus] getGLExtensions() erro: " + t.getMessage());
            return "";
        }
    }

    /**
     * Retorna extensões EGL do driver nativo.
     * Retorna string vazia em caso de erro.
     */
    public static String getEGLExtensionsSafe() {
        if (!loaded) return "";
        try {
            String result = getEGLExtensions();
            return result != null ? result : "";
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("[Nexus] getEGLExtensions() não disponível: " + ule.getMessage());
            return "";
        } catch (Throwable t) {
            System.err.println("[Nexus] getEGLExtensions() erro: " + t.getMessage());
            return "";
        }
    }

    /**
     * Retorna extensões de áudio nativas.
     * Retorna string vazia em caso de erro.
     */
    public static String getAudioExtensionsSafe() {
        if (!loaded) return "";
        try {
            String result = getAudioExtensions();
            return result != null ? result : "";
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("[Nexus] getAudioExtensions() não disponível: " + ule.getMessage());
            return "";
        } catch (Throwable t) {
            System.err.println("[Nexus] getAudioExtensions() erro: " + t.getMessage());
            return "";
        }
    }

    /**
     * Retorna extensões Vulkan disponíveis.
     * Retorna string vazia em caso de erro.
     */
    public static String getVulkanExtensionsSafe() {
        if (!loaded) return "";
        try {
            String result = getVulkanExtensions();
            return result != null ? result : "";
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("[Nexus] getVulkanExtensions() não disponível: " + ule.getMessage());
            return "";
        } catch (Throwable t) {
            System.err.println("[Nexus] getVulkanExtensions() erro: " + t.getMessage());
            return "";
        }
    }

    /**
     * Shutdown seguro. Nunca lança exceção.
     */
    public static void shutdownSafe() {
        if (!loaded) return;
        try {
            shutdownNexusCore();
            System.out.println("[Nexus] Core shutdown OK.");
        } catch (UnsatisfiedLinkError ule) {
            System.err.println("[Nexus] shutdownNexusCore() não disponível: " + ule.getMessage());
        } catch (Throwable t) {
            System.err.println("[Nexus] Erro durante shutdown: " + t.getMessage());
        }
    }

    // ── Métodos nativos — não chamar diretamente, usar os Safe acima ──────────

    public static native boolean initNexusCore();
    public static native String  getGLExtensions();
    public static native String  getEGLExtensions();
    public static native String  getVulkanExtensions();
    public static native String  getAudioExtensions();
    public static native void    shutdownNexusCore();
}
