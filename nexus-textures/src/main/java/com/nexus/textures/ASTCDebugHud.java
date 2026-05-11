package com.nexus.textures;

import java.util.List;

public class ASTCDebugHud {
    public static void addLeftLines(List<String> lines) {
        lines.add("");
        boolean active = ASTCTextureRegistry.count() > 0;
        boolean encoderOk = ASTCEncoder.isAvailable();

        lines.add("[Nexus ASTC] " + (active ? "ON" : "OFF"));
        if (active) {
            lines.add("  Pre-ASTC: " + ASTCTextureRegistry.count() + " texturas");
            lines.add("  Encoder ARM64: " + (encoderOk ? "ON" : "OFF"));
            lines.add("  Uploads runtime: " + ASTCLoadingState.getRuntimeUploads());
            if (ASTCLoadingState.isDone() && ASTCLoadingState.getLoadTimeMs() > 0) {
                lines.add("  Carregadas em: " + ASTCLoadingState.getLoadTimeMs() + " ms");
            }
        }
    }
}