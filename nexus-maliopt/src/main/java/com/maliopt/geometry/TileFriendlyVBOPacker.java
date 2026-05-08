package com.maliopt.geometry;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Junta geometria de vários chunks num único VBO grande,
 * que o Mali TDBR adora (menos chamadas GL, menos overdraw).
 */
public class TileFriendlyVBOPacker {
    private static final int MAX_VBO_SIZE = 4 * 1024 * 1024; // 4 MB
    private static int currentVbo = 0;
    private static ByteBuffer currentBuffer = ByteBuffer.allocateDirect(MAX_VBO_SIZE);
    private static int currentOffset = 0;
    private static final List<Integer> allVbos = new ArrayList<>();

    public static void reset() {
        if (currentVbo != 0) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, currentVbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, currentBuffer.flip(), GL15.GL_STATIC_DRAW);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
            allVbos.add(currentVbo);
        }
        currentBuffer = ByteBuffer.allocateDirect(MAX_VBO_SIZE);
        currentVbo = 0;
        currentOffset = 0;
    }

    public static int getCurrentVbo() {
        if (currentVbo == 0) {
            currentVbo = GL15.glGenBuffers();
        }
        return currentVbo;
    }

    public static int appendGeometry(ByteBuffer data) {
        if (currentOffset + data.remaining() > MAX_VBO_SIZE) {
            reset();
        }
        int vbo = getCurrentVbo();
        currentBuffer.put(data);
        int offset = currentOffset;
        currentOffset += data.remaining();
        return offset;
    }

    public static void finalizeVbo() {
        reset();
    }

    public static void clear() {
        for (int vbo : allVbos) GL15.glDeleteBuffers(vbo);
        allVbos.clear();
        reset();
    }
}
