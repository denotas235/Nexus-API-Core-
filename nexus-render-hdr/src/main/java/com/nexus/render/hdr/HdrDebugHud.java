package com.nexus.render.hdr;

import java.util.List;

public class HdrDebugHud {
    public static void addLeftLines(List<String> lines) {
        lines.add("");
        boolean ready = HdrPipeline.isReady();
        lines.add("[Nexus HDR] " + (ready ? "ON" : "A inicializar..."));
        if (ready) {
            lines.add("  sRGB framebuffer: " + (HdrPipeline.hasSRGB()       ? "ON \u2714" : "OFF \u2718"));
            lines.add("  Anisotropic:      " + (HdrPipeline.hasAnisotropic() ? "ON \u2714  (" + (int)HdrPipeline.getMaxAnisotropy() + "x)" : "OFF \u2718"));
            lines.add("  ACES Tonemapping: " + (HdrPipeline.hasACES()        ? "compilado \u2714" : "OFF \u2718"));
        }
    }
}