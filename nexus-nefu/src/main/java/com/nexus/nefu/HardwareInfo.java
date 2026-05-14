package com.nexus.nefu;

import net.fabricmc.loader.api.FabricLoader;
import org.lwjgl.opengl.GL11;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Collects GPU / OpenGL information via LWJGL and writes a JSON profile to
 * &lt;game-dir&gt;/nefu_device_profile.json for diagnostics.
 *
 * MUST be called after the GL context is established
 * (i.e. inside ClientLifecycleEvents.CLIENT_STARTED).
 */
public class HardwareInfo {

    private static String renderer   = "unknown";
    private static String vendor     = "unknown";
    private static String glVersion  = "unknown";
    private static String extensions = "";

    public static void collectAndSave() {
        try {
            gather();
            saveJson();
        } catch (Exception e) {
            System.err.println("[NEFU] HardwareInfo: collection failed — " + e.getMessage());
        }
    }

    public static String  getRenderer()              { return renderer; }
    public static String  getVendor()                { return vendor; }
    public static String  getGlVersion()             { return glVersion; }
    public static boolean hasExtension(String ext)   { return !ext.isEmpty() && extensions.contains(ext); }

    // ── Private ───────────────────────────────────────────────────────────────
    private static void gather() {
        String r = GL11.glGetString(GL11.GL_RENDERER);
        renderer = (r != null) ? r : "unknown";

        String v = GL11.glGetString(GL11.GL_VENDOR);
        vendor = (v != null) ? v : "unknown";

        String g = GL11.glGetString(GL11.GL_VERSION);
        glVersion = (g != null) ? g : "unknown";

        // GL 3.x core profile may return null; that is acceptable.
        String e = GL11.glGetString(GL11.GL_EXTENSIONS);
        extensions = (e != null) ? e : "";

        System.out.println("[NEFU] GPU : " + vendor + " / " + renderer);
        System.out.println("[NEFU] GL  : " + glVersion);
        System.out.println("[NEFU] Tier: T" + TierManager.getTier());
    }

    private static void saveJson() {
        Path out = FabricLoader.getInstance().getGameDir()
                .resolve("nefu_device_profile.json");
        try (BufferedWriter bw = Files.newBufferedWriter(out)) {
            String[] exts = extensions.isEmpty() ? new String[0] : extensions.split(" ");
            bw.write("{");
            bw.newLine();
            bwLine(bw, "  \"renderer\": \"" + esc(renderer) + "\",");
            bwLine(bw, "  \"vendor\": \"" + esc(vendor) + "\",");
            bwLine(bw, "  \"glVersion\": \"" + esc(glVersion) + "\",");
            bwLine(bw, "  \"detectedTier\": " + TierManager.getTier() + ",");
            bwLine(bw, "  \"tbdrFramebufferFetch\": " + NefuCoreEngine.hasTbdrFramebufferFetch() + ",");
            bwLine(bw, "  \"tbdrBufferStorage\": " + NefuCoreEngine.hasTbdrBufferStorage() + ",");
            bwLine(bw, "  \"extensionCount\": " + exts.length + ",");
            bw.write("  \"extensions\": [");
            bw.newLine();
            for (int i = 0; i < exts.length; i++) {
                bw.write("    \"" + esc(exts[i]) + "\"" + (i < exts.length - 1 ? "," : ""));
                bw.newLine();
            }
            bwLine(bw, "  ]");
            bw.write("}");
            bw.newLine();
            System.out.println("[NEFU] Device profile -> " + out);
        } catch (IOException ex) {
            System.err.println("[NEFU] HardwareInfo: write failed — " + ex.getMessage());
        }
    }

    private static void bwLine(BufferedWriter bw, String s) throws IOException {
        bw.write(s); bw.newLine();
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
