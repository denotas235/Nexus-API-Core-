package com.nexus.shadows;

import org.lwjgl.opengl.*;

public class ShadowPCFShader {
    private static final String VERT =
        "#version 310 es\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n" +
        "    vUv = uv;\n" +
        "    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);\n" +
        "}\n";

    private static final String FRAG =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D uScene;\n" +
        "uniform sampler2D uShadowMap;\n" +
        "uniform vec2 uScreenSize;\n" +
        "in vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "float PCF(sampler2D shadowMap, vec2 uv, float depth) {\n" +
        "    float shadow = 0.0;\n" +
        "    vec2 texelSize = 1.0 / vec2(1024.0);\n" +
        "    for (int x = -1; x <= 1; x++) {\n" +
        "        for (int y = -1; y <= 1; y++) {\n" +
        "            float pcfDepth = texture(shadowMap, uv + vec2(x, y) * texelSize).r;\n" +
        "            shadow += depth - 0.001 > pcfDepth ? 0.5 : 0.0;\n" +
        "        }\n" +
        "    }\n" +
        "    return shadow / 9.0;\n" +
        "}\n" +
        "void main() {\n" +
        "    vec3 color = texture(uScene, vUv).rgb;\n" +
        "    float depth = texture(uShadowMap, vUv).r;\n" +
        "    float shadow = PCF(uShadowMap, vUv, depth);\n" +
        "    color *= 1.0 - shadow;\n" +
        "    fragColor = vec4(color, 1.0);\n" +
        "}\n";

    private static int program = 0;
    private static int quadVao = 0;

    public static void compile() {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, VERT);
            GL20.glCompileShader(vert);
            if (GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.out.println("[Shadows] PCF vertex error: " + GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert);
                return;
            }
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, FRAG);
            GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.out.println("[Shadows] PCF fragment error: " + GL20.glGetShaderInfoLog(frag));
                GL20.glDeleteShader(vert);
                GL20.glDeleteShader(frag);
                return;
            }
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vert);
            GL20.glAttachShader(program, frag);
            GL20.glLinkProgram(program);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                System.out.println("[Shadows] PCF link error: " + GL20.glGetProgramInfoLog(program));
                GL20.glDeleteProgram(program);
                program = 0;
            } else {
                quadVao = GL30.glGenVertexArrays();
                System.out.println("[Shadows] PCF shader compiled (program " + program + ")");
            }
        } catch (Exception e) {
            System.out.println("[Shadows] PCF exception: " + e.getMessage());
        }
    }

    public static int getProgram() { return program; }
    public static int getQuadVao() { return quadVao; }
}
