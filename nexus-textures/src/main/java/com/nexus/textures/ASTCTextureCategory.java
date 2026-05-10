package com.nexus.textures;

public enum ASTCTextureCategory {

    // formato, blockX, blockY, srgb, hdr
    TERRAIN       (0x93B0, 4,  4,  true,  false),  // ASTC 4x4 SRGB
    ENTITY        (0x93B0, 4,  4,  true,  false),  // ASTC 4x4 SRGB
    ITEM          (0x93B0, 4,  4,  true,  false),  // ASTC 4x4 SRGB
    GUI           (0x93B0, 4,  4,  true,  false),  // ASTC 4x4 SRGB
    BLOCK         (0x93B3, 5,  5,  true,  false),  // ASTC 5x5 SRGB
    PARTICLE      (0x93B7, 8,  8,  true,  false),  // ASTC 8x8 SRGB
    FLORA         (0x93B7, 8,  8,  true,  false),  // ASTC 8x8 SRGB
    CLOUD         (0x93BD, 10, 10, true,  false),  // ASTC 10x10 SRGB
    SKYBOX        (0x93BF, 12, 12, true,  false),  // ASTC 12x12 SRGB
    NORMAL_MAP    (0x93B0, 4,  4,  false, false),  // ASTC 4x4 UNORM
    EMISSIVE      (0x93B0, 4,  4,  false, true),   // ASTC 4x4 HDR
    DEFAULT       (0x93B0, 4,  4,  true,  false);  // ASTC 4x4 SRGB fallback

    public final int glFormat;
    public final int blockX;
    public final int blockY;
    public final boolean srgb;
    public final boolean hdr;

    ASTCTextureCategory(int glFormat, int blockX, int blockY, boolean srgb, boolean hdr) {
        this.glFormat = glFormat;
        this.blockX   = blockX;
        this.blockY   = blockY;
        this.srgb     = srgb;
        this.hdr      = hdr;
    }

    public static ASTCTextureCategory fromPath(String path) {
        if (path == null) return DEFAULT;
        String p = path.toLowerCase();

        if (p.contains("atlas")              ) return TERRAIN;
        if (p.contains("textures/block")     ) return BLOCK;
        if (p.contains("textures/entity")    ) return ENTITY;
        if (p.contains("textures/item")      ) return ITEM;
        if (p.contains("textures/gui")       ) return GUI;
        if (p.contains("textures/particle")  ) return PARTICLE;
        if (p.contains("textures/environment")) return SKYBOX;
        if (p.contains("textures/effect")    ) return EMISSIVE;
        if (p.contains("normal")             ) return NORMAL_MAP;
        if (p.contains("emissive")           ) return EMISSIVE;
        if (p.contains("cloud")              ) return CLOUD;
        if (p.contains("foliage")            ) return FLORA;
        if (p.contains("grass")              ) return FLORA;
        if (p.contains("leaves")             ) return FLORA;

        return DEFAULT;
    }

    public int getGLInternalFormat() {
        if (hdr)  return 0x93D0 + getASTCIndex(); // KHR_astc_hdr offset
        if (srgb) return 0x93D0 + getASTCIndex(); // sRGB offset
        return glFormat;
    }

    private int getASTCIndex() {
        return switch (blockX * 100 + blockY) {
            case 404  -> 0;
            case 504  -> 1;
            case 505  -> 2;
            case 605  -> 3;
            case 606  -> 4;
            case 805  -> 5;
            case 806  -> 6;
            case 808  -> 7;
            case 1005 -> 8;
            case 1006 -> 9;
            case 1008 -> 10;
            case 1010 -> 11;
            case 1210 -> 12;
            case 1212 -> 13;
            default   -> 0;
        };
    }
}
