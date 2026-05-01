package com.nexuapicore.core.nativelink;

import java.io.*;
import java.nio.file.*;

public class NexusNativeLoader {
    public static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        try {
            String arch = System.getProperty("os.arch").contains("64") ? "arm64-v8a" : "armeabi-v7a";
            String libName = "libnexus_mali_core.so";
            InputStream in = NexusNativeLoader.class.getResourceAsStream("/natives/" + arch + "/" + libName);
            if (in == null)
                throw new RuntimeException("Native lib not found: /natives/" + arch + "/" + libName);

            Path tmpDir = Files.createTempDirectory("nexus_natives");
            Path tmpLib = tmpDir.resolve(libName);
            Files.copy(in, tmpLib, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.load(tmpLib.toAbsolutePath().toString());
            loaded = true;
            System.out.println("[Nexus] Core library loaded: " + tmpLib);

            if (initNexusCore()) {
                System.out.println("[Nexus] Nexus Mali Core initialized.");
            } else {
                System.err.println("[Nexus] Nexus Mali Core init failed.");
            }
        } catch (Exception e) {
            System.err.println("[Nexus] Failed to load native lib: " + e.getMessage());
        }
    }

    public static native boolean initNexusCore();
    public static native String getGLExtensions();
    public static native String getEGLExtensions();
    public static native String getVulkanExtensions();
    public static native String getAudioExtensions();
    public static native void shutdownNexusCore();
}
