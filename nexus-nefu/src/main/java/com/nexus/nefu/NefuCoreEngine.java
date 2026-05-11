package com.nexus.nefu;

public class NefuCoreEngine {
    public enum Renderer { LTW, MOBILEGLUES, KRYPTON, ZINK, VIRGL, PASSTHROUGH }
    private static Renderer current = Renderer.LTW;
    private static boolean active = true;

    public static void init() {
        try {
            System.loadLibrary("nefu");
            nativeInit();
            active = true;
            System.out.println("[NEFU] Core engine initialized. Current renderer: " + current);
        } catch (Throwable t) {
            active = false;
            current = Renderer.PASSTHROUGH;
            System.err.println("[NEFU] Failed to load native library – disabling NEFU. Launcher renderer will be used instead.");
        }
    }
    public static void selectRenderer(Renderer r) { current = r; if (active) nativeSelectRenderer(r.ordinal()); }
    public static Renderer getCurrentRenderer() { return current; }
    public static boolean isActive() { return active; }
    private static native boolean nativeInit();
    private static native void nativeSelectRenderer(int id);
}
