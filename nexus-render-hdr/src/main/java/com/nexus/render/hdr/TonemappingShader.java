package com.nexus.render.hdr;

import org.lwjgl.opengl.*;

public class TonemappingShader {
    // Vertex shader optimizado para ES 3.2
    private static final String VERT =
        "#version 310 es\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n" +
        "    vUv = uv;\n" +
        "    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);\n" +
        "}\n";

    // Fragment shader ACES para ES 3.2
    private static final String FRAG =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D uScene;\n" +
        "in vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "vec3 aces_tonemap(vec3 x) {\n" +
        "    const float a = 2.51;\n" +
        "    const float b = 0.03;\n" +
        "    const float c = 2.43;\n" +
        "    const float d = 0.59;\n" +
        "    const float e = 0.14;\n" +
        "    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);\n" +
        "}\n" +
        "void main() {\n" +
        "    vec3 color = texture(uScene, vUv).rgb;\n" +
        "    color = aces_tonemap(color);\n" +
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
                System.out.println("[HDR] Vertex shader error: " + GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert);
                return;
            }
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, FRAG);
            GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.out.println("[HDR] Fragment shader error: " + GL20.glGetShaderInfoLog(frag));
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
                System.out.println("[HDR] Program link error: " + GL20.glGetProgramInfoLog(program));
                GL20.glDeleteProgram(program);
                program = 0;
            } else {
                quadVao = GL30.glGenVertexArrays();
                System.out.println("[HDR] Tonemapping shader compiled for ES 3.2 (program " + program + ")");
            }
        } catch (Exception e) {
            System.out.println("[HDR] Tonemapping shader exception: " + e.getMessage());
        }
    }

    public static int getProgram() { return program; }
    public static int getQuadVao() { return quadVao; }
}
