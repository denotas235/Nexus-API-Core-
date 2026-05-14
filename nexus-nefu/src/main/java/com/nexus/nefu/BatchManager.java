package com.nexus.nefu;

import java.util.ArrayList;
import java.util.List;

public class BatchManager {
    private static boolean enabled = true;
    private static int currentMode = -1;
    private static final List<int[]> pending = new ArrayList<>();
    private static final int MAX_VERTICES = 65536;

    public static void setEnabled(boolean en) {
        enabled = en;
        if (!en) flush();
    }

    public static void queue(int mode, int first, int count) {
        if (!enabled || !NefuCoreEngine.isActive()) {
            directDraw(mode, first, count);
            return;
        }
        if (mode != currentMode) {
            flush();
            currentMode = mode;
        }
        pending.add(new int[]{first, count});
        int totalVerts = pending.stream().mapToInt(a -> a[1]).sum();
        if (totalVerts >= MAX_VERTICES) {
            flush();
        }
    }

    public static void flush() {
        if (pending.isEmpty()) return;
        int[] firsts = new int[pending.size()];
        int[] counts = new int[pending.size()];
        for (int i = 0; i < pending.size(); i++) {
            firsts[i] = pending.get(i)[0];
            counts[i] = pending.get(i)[1];
        }
        NefuCoreEngine.nativeDrawArraysBatched(currentMode, firsts, counts);
        pending.clear();
        currentMode = -1;
    }

    private static void directDraw(int mode, int first, int count) {
        com.mojang.blaze3d.systems.RenderSystem.drawArrays(mode, first, count);
    }
}
