package com.nexus.shadows;

import org.lwjgl.opengl.*;

public class ShadowPCFShader {

    private static final String VERT_CORE =
        "#version 330 core\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv=vec2((gl_VertexID<<1)&2,gl_VertexID&2);\n" +
        "    vUv=uv; gl_Position=vec4(uv*2.0-1.0,0.0,1.0);\n" +
        "}\n";

    private static final String FRAG_CORE =
        "#version 330 core\n" +
        "uniform sampler2D uScene;\n" +
        "uniform sampler2D uShadowMap;\n" +
        "uniform vec2 uScreenSize;\n" +
        "in vec2 vUv; out vec4 fragColor;\n" +
        "float PCF(sampler2D sm, vec2 uv, float d) {\n" +
        "    float s=0.0; vec2 tx=1.0/vec2(1024.0);\n" +
        "    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++)\n" +
        "        s+=(d-0.001>texture(sm,uv+vec2(x,y)*tx).r)?0.5:0.0;\n" +
        "    return s/9.0;\n" +
        "}\n" +
        "void main() {\n" +
        "    vec3 c=texture(uScene,vUv).rgb;\n" +
        "    float sh=PCF(uShadowMap,vUv,texture(uShadowMap,vUv).r);\n" +
        "    fragColor=vec4(c*(1.0-sh*0.6),1.0);\n" +
        "}\n";

    private static final String VERT_ES =
        "#version 310 es\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv=vec2((gl_VertexID<<1)&2,gl_VertexID&2);\n" +
        "    vUv=uv; gl_Position=vec4(uv*2.0-1.0,0.0,1.0);\n" +
        "}\n";

    private static final String FRAG_ES =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D uScene;\n" +
        "uniform sampler2D uShadowMap;\n" +
        "uniform vec2 uScreenSize;\n" +
        "in vec2 vUv; out vec4 fragColor;\n" +
        "float PCF(sampler2D sm, vec2 uv, float d) {\n" +
        "    float s=0.0; vec2 tx=1.0/vec2(1024.0);\n" +
        "    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++)\n" +
        "        s+=(d-0.001>texture(sm,uv+vec2(float(x),float(y))*tx).r)?0.5:0.0;\n" +
        "    return s/9.0;\n" +
        "}\n" +
        "void main() {\n" +
        "    vec3 c=texture(uScene,vUv).rgb;\n" +
        "    float sh=PCF(uShadowMap,vUv,texture(uShadowMap,vUv).r);\n" +
        "    fragColor=vec4(c*(1.0-sh*0.6),1.0);\n" +
        "}\n";

    private static int program = 0;
    private static int quadVao = 0;

    public static void compile() {
        if (tryCompile(VERT_CORE, FRAG_CORE, "330 core")) return;
        if (tryCompile(VERT_ES,   FRAG_ES,   "310 es"))   return;
        NexusShadowsClient.LOGGER.warn("[Shadows] ShadowPCFShader nao compilado.");
    }

    private static boolean tryCompile(String vs, String fs, String label) {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, vs); GL20.glCompileShader(vert);
            if (GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] PCF vert {} falhou: {}", label, GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert); return false;
            }
            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, fs); GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] PCF frag {} falhou: {}", label, GL20.glGetShaderInfoLog(frag));
                GL20.glDeleteShader(vert); GL20.glDeleteShader(frag); return false;
            }
            int prog = GL20.glCreateProgram();
            GL20.glAttachShader(prog, vert); GL20.glAttachShader(prog, frag);
            GL20.glLinkProgram(prog);
            GL20.glDeleteShader(vert); GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                NexusShadowsClient.LOGGER.debug("[Shadows] PCF link {} falhou.", label);
                GL20.glDeleteProgram(prog); return false;
            }
            program = prog;
            quadVao = GL30.glGenVertexArrays();
            NexusShadowsClient.LOGGER.info("[Shadows] ShadowPCFShader compilado ({}).", label);
            return true;
        } catch (Exception e) {
            NexusShadowsClient.LOGGER.debug("[Shadows] PCF excecao {}: {}", label, e.getMessage());
            return false;
        }
    }

    public static int getProgram() { return program; }
    public static int getQuadVao() { return quadVao; }
}