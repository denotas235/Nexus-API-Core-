package com.maliopt.config;

public class MaliOptConfig {
    // PERFORMANCE
    public static int fpsTarget = 60;
    public static int renderDistance = 8;
    public static boolean autoViewDistance = false;
    public static boolean elytraMode = true;
    public static boolean greedyMeshing = true;
    public static int greedyMaxSize = 16;
    public static boolean meshMultiThread = false; // placeholder

    // VISUAL – iluminação
    public static boolean plsEnabled = true;
    public static float warmth = 0.3f;
    public static float ambientOcclusion = 0.6f;
    public static float contrast = 0.05f;
    // bloom
    public static boolean bloomEnabled = true;
    public static float bloomThreshold = 0.3f;
    public static float bloomRadius = 1.5f;
    public static float bloomIntensity = 0.7f;
    public static int bloomQuality = 3; // 1=fast,3=normal,5=ultra
    // sombras
    public static boolean shadowsEnabled = true;
    public static int shadowDistance = 64;
    public static int shadowMapSize = 1024;
    public static int shadowPCFSamples = 4;
    public static float shadowBias = 0.001f;
    // tonemapping
    public static String tonemapper = "ACES";
    public static boolean hdrPipeline = true;
    public static float saturation = 1.0f;
    public static float colorTemp = 6500f;
    public static float gamma = 2.2f;
    // filtros
    public static int anisotropicFiltering = 8; // 0=off,2,4,8,16
    public static int msaa = 2; // 0=off,2,4
    public static boolean astcEnabled = true;
    public static int astcQuality = 6; // 4,6,8 (block size)
    public static boolean sRGB = true;
    // terreno
    public static boolean tessellation = false; // placeholder
    public static int lodMax = 4;
    public static boolean fogEnabled = true;
    public static float fogDensity = 0.2f;
    // entidades
    public static boolean entityLighting = true;
    public static boolean entityShadows = true;
    public static boolean smoothEntityMotion = true;
    // efeitos
    public static boolean gpuParticles = false; // placeholder
    public static int maxParticles = 1000;

    // ADVANCED – TBDR
    public static boolean glInvalidateFramebuffer = true;
    public static boolean invalidateDepthAfterShadow = true;
    public static boolean invalidateStencilAfterFrame = true;
    // shader pipeline
    public static boolean binaryShaderCache = true;
    public static int cacheMaxSizeMB = 256;
    public static boolean armProgramBinary = true;
    public static boolean plsInShaders = true;
    public static boolean fbFetchInShaders = true;
    public static boolean mediumpOptimisation = true;
    // debug
    public static boolean frameProfiler = false;
    public static boolean capabilityReportOnStart = true;
    public static boolean hudEnabled = true;
}
