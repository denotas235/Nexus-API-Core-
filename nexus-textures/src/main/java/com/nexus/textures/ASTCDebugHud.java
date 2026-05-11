package com.nexus.textures;

import java.util.List;

public class ASTCDebugHud {
    public static void addLeftLines(List<String> lines) {
        lines.add("");
        boolean active = ASTCTextureRegistry.count() > 0;
        boolean encoderOk = ASTCEncoder.isAvailable();

        lines.add("[Nexus ASTC] " + (active ? "ON \u2714" : "OFF \u2718"));
        if (active) {
            lines.add("  Pre-ASTC (JAR): " + ASTCTextureRegistry.count() + " texturas");
        }
        lines.add("  Encoder ARM64:  " + (encoderOk ? "ON \u2714" : "OFF \u2718 — resource packs sem compressao runtime"));
        if (!encoderOk && ASTCEncoder.getLoadError() != null) {
            lines.add("  ! " + ASTCEncoder.getLoadError());
        }
        lines.add("  Uploads runtime: " + ASTCLoadingState.getRuntimeUploads());
        if (ASTCLoadingState.isDone() && ASTCLoadingState.getLoadTimeMs() > 0) {
            lines.add("  Manifest carregado em: " + ASTCLoadingState.getLoadTimeMs() + " ms");
        }
    }
}