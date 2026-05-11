package com.nexus.nefu;

import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;

public class BatchManager {
    private static int lastMode = -1;
    private static final List<Integer> pendingVertices = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        System.out.println("[NEFU] BatchManager initialized.");
    }

    /**
     * Retorna true se este draw pode ser acumulado no lote actual.
     * So faz batching de draws consecutivos com o mesmo mode GL.
     */
    public static boolean shouldBatch(int mode, int count) {
        return initialized && lastMode == mode && lastMode != -1;
    }

    public static void addToBatch(int mode, int first, int count) {
        for (int i = first; i < first + count; i++) {
            pendingVertices.add(i);
        }
        lastMode = mode;
    }

    /** Envia o lote acumulado para a GPU e limpa o estado. */
    public static void flush() {
        if (!pendingVertices.isEmpty() && lastMode != -1) {
            GL11.glDrawArrays(lastMode, 0, pendingVertices.size());
            pendingVertices.clear();
        }
        lastMode = -1;
    }

    /** Reinicia o modo de batching para um novo tipo de primitivo. */
    public static void resetMode(int newMode) {
        lastMode = newMode;
        pendingVertices.clear();
    }
}

