package com.nexus.nefu;

public class NefuCoreEngine {
    public enum Renderer { LTW, MOBILEGLUES, KRYPTON, ZINK, VIRGL }
    private static Renderer current = Renderer.LTW;

    public static void init() {
        System.loadLibrary("nefu");
        nativeInit();
        System.out.println("[NEFU] Core engine initialized. Current renderer: " + current);
    }
    public static void selectRenderer(Renderer r) { current = r; nativeSelectRenderer(r.ordinal()); }
    public static Renderer getCurrentRenderer() { return current; }
    private static native boolean nativeInit();
    private static native void nativeSelectRenderer(int id);
}
