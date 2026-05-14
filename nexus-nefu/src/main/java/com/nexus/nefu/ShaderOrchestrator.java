package com.nexus.nefu;

public class ShaderOrchestrator {
    public static int selectRendererForShader(String glslSource) {
        if (glslSource.contains("#version 450") || glslSource.toLowerCase().contains("vulkan"))
            return NefuCoreEngine.RENDERER_ZINK;
        if (glslSource.contains("#version 330") || glslSource.contains("layout(binding"))
            return NefuCoreEngine.RENDERER_MOBILEGLUES;
        return NefuCoreEngine.RENDERER_LTW;
    }
}
