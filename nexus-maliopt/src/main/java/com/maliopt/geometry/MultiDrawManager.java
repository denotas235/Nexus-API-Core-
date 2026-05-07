package com.maliopt.geometry;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class MultiDrawManager {
    private static boolean enabled = false;
    private static int buf = 0;
    private static final int MAX_DRAWS = 4096;
    private static final List<int[]> drawCommands = new ArrayList<>();

    public static void init() {
        try {
            buf = GL15.glGenBuffers();
            GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, buf);
            GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, MAX_DRAWS * 20L, GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
            enabled = true;
            MaliOptMod.LOGGER.info("[SFTGS] MultiDrawManager activo (max {} draws)", MAX_DRAWS);
        } catch (Exception e) {
            MaliOptMod.LOGGER.warn("[SFTGS] MultiDrawManager não disponível: {}", e.getMessage());
            enabled = false;
        }
    }

    public static boolean isEnabled() { return enabled; }
}
