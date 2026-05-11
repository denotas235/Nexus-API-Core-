package com.nexus.nefu;

public class ShaderOrchestrator {
    public static NefuCoreEngine.Renderer selectForShader(String source) {
        if (source.contains("#version 450") || source.contains("vulkan")) {
            return NefuCoreEngine.Renderer.ZINK;
        } else if (source.contains("#version 330") || source.contains("layout(binding")) {
            return NefuCoreEngine.Renderer.MOBILEGLUES;
        }
        return NefuCoreEngine.Renderer.LTW;
    }
}
