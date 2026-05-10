package com.nexus.shadows;

import org.lwjgl.opengl.*;

public class ShadowMapShader {
    private static final String VERT =
        "#version 310 es\n" +
        "layout(location = 0) in vec3 aPos;\n" +
        "uniform mat4 uLightView;\n" +
        "void main() {\n" +
        "    gl_Position = uLightView * vec4(aPos, 1.0);\n" +
        "}\n";

    private static final String FRAG =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "void main() {\n" +
        "    // Apenas escreve depth automaticamente\n" +
        "}\n";

    private static int program = 0;

    public static void compile() {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, VERT);
            GL20.glCompileShader(vert);
            if (GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.out.println("[Shadows] ShadowMap vertex error: " + GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert);
                return;
            }
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, FRAG);
            GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                System.out.println("[Shadows] ShadowMap fragment error: " + GL20.glGetShaderInfoLog(frag));
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
                System.out.println("[Shadows] ShadowMap link error: " + GL20.glGetProgramInfoLog(program));
                GL20.glDeleteProgram(program);
                program = 0;
            } else {
                System.out.println("[Shadows] ShadowMap shader compiled.");
            }
        } catch (Exception e) {
            System.out.println("[Shadows] ShadowMap exception: " + e.getMessage());
        }
    }

    public static int getProgram() { return program; }
}
