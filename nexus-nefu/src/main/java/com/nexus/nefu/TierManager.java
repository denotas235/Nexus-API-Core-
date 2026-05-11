package com.nexus.nefu;

public class TierManager {
    public enum Tier { T0, T1, T2, T3, T4, T5 }
    private static Tier current = Tier.T2;

    public static Tier detectTier() {
        String gl = org.lwjgl.opengl.GL11.glGetString(org.lwjgl.opengl.GL11.GL_RENDERER);
        if (gl != null && gl.contains("Mali-G52")) return Tier.T2;
        return Tier.T2;
    }
    public static Tier getCurrentTier() { return current; }
}
