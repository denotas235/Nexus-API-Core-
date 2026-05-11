package com.nexus.nefu;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BatchManager {
    private static int lastMode = -1;
    private static final List<Integer> pendingVertices = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        System.out.println("[NEFU] BatchManager initialized.");
    }

    public static boolean shouldBatch(int mode, int count) {
        return mode == lastMode;
    }

    public static void addToBatch(int mode, int first, int count) {
        pendingVertices.addAll(IntStream.range(first, first + count).boxed().collect(Collectors.toList()));
        lastMode = mode;
    }

    public static void flush() {
        if (!pendingVertices.isEmpty()) {
            GL11.glDrawArrays(lastMode, 0, pendingVertices.size());
            pendingVertices.clear();
        }
    }
}
