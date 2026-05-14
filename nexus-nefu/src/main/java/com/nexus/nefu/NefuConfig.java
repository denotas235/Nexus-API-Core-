package com.nexus.nefu;

/**
 * User-facing configuration for NEFU.
 * All fields have safe defaults so the mod works out-of-the-box.
 */
public class NefuConfig {

    // Core
    public boolean batchingEnabled         = true;
    public int     tierOverride            = -1;   // -1 = auto-detect
    public boolean hdr                     = false;
    public int     shadowQuality           = 1;    // 0-3
    public boolean useZink                 = false;

    // MobileGlues
    public boolean mobilegluesEnableAngle     = false;
    public int     mobilegluesMaxGlslCacheMb  = 32;
    public String  mobilegluesCustomGLVersion = "4.0.0";
    public boolean mobilegluesShaderDebug     = false;

    // TBDR / Mali
    public boolean tbdrFramebufferFetch = true;
    public boolean tbdrBufferStorage    = true;
}
