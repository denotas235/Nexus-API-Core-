package com.maliopt.world;

import com.maliopt.MaliOptMod;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;

public class CaveCarver {
    private static int program = 0;
    private static boolean ready = false;
    private static int caveTex = 0;
    private static final int SIZE = 64;

    private static final String COMP =
        "#version 310 es\n" +
        "layout(local_size_x = 8, local_size_y = 8, local_size_z = 8) in;\n" +
        "layout(rgba32f) uniform highp image3D uDensity;\n" +
        "float hash(vec3 p){return fract(sin(dot(p,vec3(127.1,311.7,74.7)))*43758.5453);}\n" +
        "float noise3D(vec3 p){\n" +
        "    vec3 i=floor(p); vec3 f=fract(p);\n" +
        "    f=f*f*(3.0-2.0*f);\n" +
        "    return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),\n" +
        "               mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),\n" +
        "               mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),\n" +
        "               mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);\n" +
        "}\n" +
        "void main(){\n" +
        "    ivec3 p=ivec3(gl_GlobalInvocationID.xyz);\n" +
        "    vec3 uv=vec3(p)/float({0});\n" +
        "    float d=noise3D(uv*8.0)-0.5;\n" +
        "    imageStore(uDensity,p,vec4(d,0,0,1));\n" +
        "}\n";

    public static void init() {
        try {
            String src = COMP.replace("{0}", String.valueOf(SIZE));
            int s = GL20.glCreateShader(0x91B9);
            GL20.glShaderSource(s, src);
            GL20.glCompileShader(s);
            if (GL20.glGetShaderi(s, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                MaliOptMod.LOGGER.error("[CaveCarver] Shader fail: {}", GL20.glGetShaderInfoLog(s));
                return;
            }
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, s);
            GL20.glLinkProgram(program);
            GL20.glDeleteShader(s);

            caveTex = GL11.glGenTextures();
            GL11.glBindTexture(GL12.GL_TEXTURE_3D, caveTex);
            GL12.glTexImage3D(GL12.GL_TEXTURE_3D, 0, GL30.GL_RGBA32F, SIZE, SIZE, SIZE, 0, GL30.GL_RGBA, GL11.GL_FLOAT, (ByteBuffer)null);
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glBindTexture(GL12.GL_TEXTURE_3D, 0);
            ready = true;
            MaliOptMod.LOGGER.info("[CaveCarver] Vulkan‑style cave generator pronto");
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[CaveCarver] init falhou: {}", e.getMessage());
        }
    }

    public static boolean isReady() { return ready; }
}
