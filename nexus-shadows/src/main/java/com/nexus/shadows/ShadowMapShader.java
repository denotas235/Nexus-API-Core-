package com.nexus.shadows;

import org.lwjgl.opengl.*;

public class ShadowMapShader {

    private static final String VERT_CORE =
        "#version 330 core\n" +
        "layout(location=0) in vec3 aPos;\n" +
        "uniform mat4 uLightSpaceMatrix;\n" +
        "void main() { gl_Position = uLightSpaceMatrix * vec4(aPos,1.0); }\n";

    private static final String FRAG_CORE =
        "#version 330 core\n" +
        "void main() {}\n";

    private static final String VERT_ES =
        "#version 310 es\n" +
        "layout(location=0) in vec3 aPos;\n" +
        "uniform mat4 uLightSpaceMatrix;\n" +
        "void main() { gl_Position = uLightSpaceMatrix * vec4(aPos,1.0); }\n";

    private static final String FRAG_ES =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "void main() {}\n";

    private static int program = 0;

    public static void compile() {
        if (tryCompile(VERT_CORE, FRAG_CORE, "330 core")) return;
        if (tryCompile(VERT_ES,   FRAG_ES,   "310 es"))   return;
        NexusShadowsClient.LOGGER.warn("[Shadows] ShadowMapShader nao compilado.");
    }

    private static boolean tryCompile(String vs, String fs, String label) {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, vs);
            GL20.glCompileShader(vert);
            if (GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] ShadowMap vert {} falhou: {}", label, GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert); return false;
            }
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, fs);
            GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] ShadowMap frag {} falhou: {}", label, GL20.glGetShaderInfoLog(frag));
                GL20.glDeleteShader(vert); GL20.glDeleteShader(frag); return false;
            }
            int prog = GL20.glCreateProgram();
            GL20.glAttachShader(prog, vert); GL20.glAttachShader(prog, frag);
            GL20.glLinkProgram(prog);
            GL20.glDeleteShader(vert); GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] ShadowMap link {} falhou.", label);
                GL20.glDeleteProgram(prog); return false;
            }
            program = prog;
            NexusShadowsClient.LOGGER.info("[Shadows] ShadowMapShader compilado ({}).", label);
            return true;
        } catch (Exception e) {
            NexusShadowsClient.LOGGER.debug("[Shadows] ShadowMap excecao {}: {}", label, e.getMessage());
            return false;
        }
    }

    public static int getProgram() { return program; }
}