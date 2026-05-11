package com.nexus.shadows;

import java.util.List;

public class ShadowDebugHud {
    public static void addLeftLines(List<String> lines) {
        lines.add("");
        boolean ready = ShadowPipeline.isReady();
        lines.add("[Nexus Shadows] " + (ready ? "ON" : "A inicializar..."));
        if (ready) {
            lines.add("  Shadow Map:   1024x1024  \u2714");
            lines.add("  PCF 3x3:      " + (ShadowPCFShader.getProgram() != 0 ? "ON \u2714" : "OFF \u2718"));
            lines.add("  ShadowShader: " + (ShadowMapShader.getProgram() != 0 ? "compilado \u2714" : "OFF \u2718"));
            lines.add("  Direcao luz:  sol (" + ShadowPipeline.getSunAngleDeg() + "\u00b0)");
        }
    }
}