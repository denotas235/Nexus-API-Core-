package com.nexus.nefu;

public class FallbackHandler {
    private static final int[] FALLBACK_ORDER = {
        NefuCoreEngine.RENDERER_MOBILEGLUES,
        NefuCoreEngine.RENDERER_KRYPTON,
        NefuCoreEngine.RENDERER_LTW,
        NefuCoreEngine.RENDERER_PASSTHROUGH
    };

    public static int getNext(int failedRenderer) {
        for (int i = 0; i < FALLBACK_ORDER.length - 1; i++) {
            if (FALLBACK_ORDER[i] == failedRenderer) return FALLBACK_ORDER[i + 1];
        }
        return NefuCoreEngine.RENDERER_PASSTHROUGH;
    }
}
