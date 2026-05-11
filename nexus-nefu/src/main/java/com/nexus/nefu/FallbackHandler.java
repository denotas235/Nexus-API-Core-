package com.nexus.nefu;

public class FallbackHandler {
    public static NefuCoreEngine.Renderer handleFailure(NefuCoreEngine.Renderer failed) {
        if (failed == NefuCoreEngine.Renderer.MOBILEGLUES) return NefuCoreEngine.Renderer.KRYPTON;
        if (failed == NefuCoreEngine.Renderer.KRYPTON) return NefuCoreEngine.Renderer.LTW;
        return NefuCoreEngine.Renderer.LTW;
    }
}
