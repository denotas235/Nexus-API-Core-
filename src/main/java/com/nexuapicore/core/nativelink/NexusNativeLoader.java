package com.nexuapicore.core.nativelink;

import java.io.*;
import java.nio.file.*;

public class NexusNativeLoader {
    public static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        try {
            String arch = System.getProperty("os.arch").contains("64") ? "arm64-v8a" : "armeabi-v7a";
            String libName = "libastc_bridge_64.so";
            InputStream in = NexusNativeLoader.class.getResourceAsStream(
                "/natives/" + arch + "/" + libName);
            if (in == null)
                throw new RuntimeException("Native lib not found in JAR: /natives/" + arch + "/" + libName);

            Path tmpDir = Files.createTempDirectory("nexus_natives");
            Path tmpLib = tmpDir.resolve(libName);
            Files.copy(in, tmpLib, StandardCopyOption.REPLACE_EXISTING);
            in.close();
            System.load(tmpLib.toAbsolutePath().toString());
            loaded = true;
            System.out.println("[Nexus] Native library loaded: " + tmpLib);
        } catch (Exception e) {
            System.err.println("[Nexus] Failed to load native library: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static native boolean initASTC();
    public static native void uploadASTC(int target, int level, int format, int w, int h, int size, ByteBuffer buffer);
}
