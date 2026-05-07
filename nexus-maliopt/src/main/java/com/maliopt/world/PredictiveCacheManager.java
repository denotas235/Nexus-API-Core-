package com.maliopt.world;

import com.maliopt.MaliOptMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import java.util.*;

public class PredictiveCacheManager {
    public enum CacheLevel { HOT, WARM, COLD }

    private static final Map<Long, CacheLevel> chunkLevels = new HashMap<>();
    private static final int HOT_RADIUS = 4;
    private static final int WARM_RADIUS = 8;

    public static void update() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;

        ChunkPos center = new ChunkPos(mc.player.getBlockPos());
        Vec3d vel = mc.player.getVelocity();

        int dx = (int) Math.round(vel.x / 16.0);
        int dz = (int) Math.round(vel.z / 16.0);

        for (int cx = center.x - WARM_RADIUS; cx <= center.x + WARM_RADIUS; cx++) {
            for (int cz = center.z - WARM_RADIUS; cz <= center.z + WARM_RADIUS; cz++) {
                long key = ChunkPos.toLong(cx, cz);
                int dist = Math.abs(cx - center.x) + Math.abs(cz - center.z);
                int dirDist = (cx - center.x) * dx + (cz - center.z) * dz;

                if (dist <= HOT_RADIUS && dirDist >= 0) {
                    chunkLevels.put(key, CacheLevel.HOT);
                } else if (dist <= WARM_RADIUS) {
                    chunkLevels.put(key, CacheLevel.WARM);
                } else {
                    chunkLevels.put(key, CacheLevel.COLD);
                }
            }
        }

        if (System.currentTimeMillis() % 10000 < 50) {
            long hot = chunkLevels.values().stream().filter(l -> l == CacheLevel.HOT).count();
            long warm = chunkLevels.values().stream().filter(l -> l == CacheLevel.WARM).count();
            MaliOptMod.LOGGER.info("[Nexus Cache] Hot: {} | Warm: {}", hot, warm);
        }
    }

    public static CacheLevel getLevel(long chunkKey) {
        return chunkLevels.getOrDefault(chunkKey, CacheLevel.COLD);
    }
}
