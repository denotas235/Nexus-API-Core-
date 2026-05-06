package com.maliopt.world;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Random;

public class HeightmapGenerator {
    private static int computeProgram = 0;
    private static int noiseTexture = 0;
    private static boolean ready = false;
    private static final int TEX_SIZE = 256;

    private static final String COMPUTE_SRC =
        "#version 310 es\n" +
        "layout(local_size_x = 16, local_size_y = 16) in;\n" +
        "layout(rgba32f) uniform highp image2D uOutput;\n" +
        "uniform float uTime;\n" +
        "float hash(vec2 p) { return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453); }\n" +
        "float noise(vec2 p) {\n" +
        "    vec2 i = floor(p); vec2 f = fract(p);\n" +
        "    f = f*f*(3.0-2.0*f);\n" +
        "    return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),\n" +
        "               mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);\n" +
        "}\n" +
        "float fbm(vec2 p) {\n" +
        "    float v=0.0,a=0.5; vec2 shift=vec2(100.0);\n" +
        "    for(int i=0;i<6;i++){ v+=a*noise(p); p=p*2.0+shift; a*=0.5; }\n" +
        "    return v;\n" +
        "}\n" +
        "void main() {\n" +
        "    ivec2 pix = ivec2(gl_GlobalInvocationID.xy);\n" +
        "    vec2 uv = vec2(pix) / float({0});\n" +
        "    float h = fbm(uv*10.0) * 60.0 + fbm(uv*3.0+50.0) * 30.0;\n" +
        "    imageStore(uOutput, pix, vec4(h, 0.0, 0.0, 1.0));\n" +
        "}\n";

    public static void init() {
        try {
            String src = COMPUTE_SRC.replace("{0}", String.valueOf(TEX_SIZE));
            int shader = GL20.glCreateShader(0x91B9);
            GL20.glShaderSource(shader, src);
            GL20.glCompileShader(shader);
            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                MaliOptMod.LOGGER.error("[Heightmap] Shader fail: {}", GL20.glGetShaderInfoLog(shader));
                return;
            }
            computeProgram = GL20.glCreateProgram();
            GL20.glAttachShader(computeProgram, shader);
            GL20.glLinkProgram(computeProgram);
            GL20.glDeleteShader(shader);

            noiseTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, TEX_SIZE, TEX_SIZE, 0, GL30.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer)null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            ready = true;
            MaliOptMod.LOGGER.info("[Heightmap] GPU compute noise generator pronto");
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[Heightmap] init falhou: {}", e.getMessage());
        }
    }

    public static float[] generate(int chunkX, int chunkZ, long seed) {
        if (!ready) return null;
        float[] data = new float[256];
        Random rand = new Random(seed ^ ((long)chunkX << 32) ^ (long)chunkZ);
        for (int i = 0; i < 256; i++) data[i] = (rand.nextFloat() * 2 - 1) * 60f;
        return data;
    }

    public static boolean isReady() { return ready; }
}
