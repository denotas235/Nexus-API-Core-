package com.nexus.render.hdr;

import org.lwjgl.opengl.*;

public class TonemappingShader {
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

    public static void compile() {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, VERT);
            GL20.glCompileShader(vert);
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, FRAG);
            GL20.glCompileShader(frag);
            int prog = GL20.glCreateProgram();
            GL20.glAttachShader(prog, vert);
            GL20.glAttachShader(prog, frag);
            GL20.glLinkProgram(prog);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                System.out.println("[HDR] Tonemapping shader link failed: " + GL20.glGetProgramInfoLog(prog));
                GL20.glDeleteProgram(prog);
            } else {
                System.out.println("[HDR] Tonemapping shader compiled (program " + prog + ")");
            }
        } catch (Exception e) {
            System.out.println("[HDR] Tonemapping shader exception: " + e.getMessage());
        }
    }
}
