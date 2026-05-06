package com.maliopt.geometry;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import java.util.HashMap;
import java.util.Map;

public class OcclusionCuller {
    private static boolean enabled = false;
    private static final Map<Integer, Integer> queryPool = new HashMap<>();
    private static int nextId = 1;

    public static void init() {
        enabled = true;
        MaliOptMod.LOGGER.info("[SFCRS] OcclusionCuller activo (GL_EXT_occlusion_query_boolean)");
    }

    public static int beginQuery() {
        if (!enabled) return -1;
        int id = nextId++;
        int queryId = GL15.glGenQueries();
        queryPool.put(id, queryId);
        GL15.glBeginQuery(GL30.GL_ANY_SAMPLES_PASSED, queryId);
        return id;
    }

    public static boolean endQuery(int queryId) {
        if (!enabled || queryId < 0) return true;
        int qid = queryPool.getOrDefault(queryId, 0);
        if (qid == 0) return true;
        GL15.glEndQuery(GL30.GL_ANY_SAMPLES_PASSED);
        int result = GL15.glGetQueryObjecti(qid, GL15.GL_QUERY_RESULT);
        GL15.glDeleteQueries(qid);
        queryPool.remove(queryId);
        return result == GL11.GL_TRUE;
    }

    public static boolean isEnabled() { return enabled; }
}
