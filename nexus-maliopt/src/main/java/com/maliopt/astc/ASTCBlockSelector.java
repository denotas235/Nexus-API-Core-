package com.maliopt.astc;

/**
 * ASTCBlockSelector — escolhe o tamanho de bloco ASTC ideal
 * baseado no caminho da textura.
 *
 * Blocos menores = mais qualidade, mais VRAM
 * Blocos maiores = menos VRAM, qualidade ligeiramente inferior
 */
public final class ASTCBlockSelector {

    private ASTCBlockSelector() {}

    public static int[] select(String texturePath) {
        String path = texturePath.toLowerCase();

        if (path.contains("textures/gui/") || path.contains("textures/item/")) {
            return new int[]{4, 4};   // 8.00 bpp — nitidez máxima para UI e itens
        }
        if (path.contains("textures/block/") || path.contains("textures/terrain/")) {
            return new int[]{5, 5};   // 5.12 bpp — equilíbrio qualidade/tamanho
        }
        if (path.contains("textures/entity/")) {
            return new int[]{5, 5};   // 5.12 bpp — entidades com detalhe moderado
        }
        if (path.contains("textures/particle/")) {
            return new int[]{8, 8};   // 2.00 bpp — partículas pequenas e rápidas
        }
        if (path.contains("textures/environment/")) {
            return new int[]{8, 5};   // 3.20 bpp — céu, sol, lua — grandes e distantes
        }
        if (path.contains("textures/colormap/")) {
            return new int[]{6, 6};   // 3.56 bpp — gradientes
        }
        if (path.contains("textures/misc/")) {
            return new int[]{6, 6};   // 3.56 bpp — genérico
        }
        // Fallback
        return new int[]{6, 6};
    }

    public static String formatName(int blockX, int blockY) {
        return blockX + "x" + blockY;
    }
}
