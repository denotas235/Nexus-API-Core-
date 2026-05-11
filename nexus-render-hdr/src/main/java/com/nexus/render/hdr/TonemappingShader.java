package com.nexus.render.hdr;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Shader ACES de tonemapping.
 *
 * Tenta compilar em GLSL 330 core (desktop OpenGL / Mesa ARM).
 * Se falhar, tenta GLSL 310 es (GLES 3.1 via ANGLE / PojavLauncher).
 * Se ambos falharem, o shader e desativado graciosamente — sem crash.
 *
 * O shader esta compilado mas inativo nesta versao.
 * Sera aplicado ao framebuffer final na v1.4.0 (ver ROADMAP).
 */
public class TonemappingShader {

    private static final String VERT_CORE =
        "#version 330 core\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n" +
        "    vUv = uv;\n" +
        "    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);\n" +
        "}\n";

    private static final String FRAG_CORE =
        "#version 330 core\n" +
        "uniform sampler2D uScene;\n" +
        "in  vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "vec3 aces(vec3 x) {\n" +
        "    const float a=2.51,b=0.03,c=2.43,d=0.59,e=0.14;\n" +
        "    return clamp((x*(a*x+b))/(x*(c*x+d)+e),0.0,1.0);\n" +
        "}\n" +
        "void main() {\n" +
        "    fragColor = vec4(aces(texture(uScene,vUv).rgb),1.0);\n" +
        "}\n";

    private static final String VERT_ES =
        "#version 310 es\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n" +
        "    vUv = uv;\n" +
        "    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);\n" +
        "}\n";

    private static final String FRAG_ES =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D uScene;\n" +
        "in  vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "vec3 aces(vec3 x) {\n" +
        "    const float a=2.51,b=0.03,c=2.43,d=0.59,e=0.14;\n" +
        "    return clamp((x*(a*x+b))/(x*(c*x+d)+e),0.0,1.0);\n" +
        "}\n" +
        "void main() {\n" +
        "    fragColor = vec4(aces(texture(uScene,vUv).rgb),1.0);\n" +
        "}\n";

    private static int program = 0;
    private static int quadVao = 0;

    public static void compile() {
        // Tentar primeiro GLSL 330 core (desktop OpenGL / Mesa)
        if (tryCompile(VERT_CORE, FRAG_CORE, "330 core")) return;
        // Fallback para GLSL 310 es (GLES / ANGLE / PojavLauncher)
        if (tryCompile(VERT_ES, FRAG_ES, "310 es")) return;
        NexusRenderHdrClient.LOGGER.warn("[NexusHDR] ACES shader nao suportado neste driver — tonemapping inativo.");
    }

    private static boolean tryCompile(String vertSrc, String fragSrc, String label) {
        try {
            int vert = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vert, vertSrc);
            GL20.glCompileShader(vert);
            if (GL20.glGetShaderi(vert, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusRenderHdrClient.LOGGER.debug("[NexusHDR] Vert {} falhou: {}", label, GL20.glGetShaderInfoLog(vert));
                GL20.glDeleteShader(vert);
                return false;
            }

            int frag = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(frag, fragSrc);
            GL20.glCompileShader(frag);
            if (GL20.glGetShaderi(frag, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                NexusRenderHdrClient.LOGGER.debug("[NexusHDR] Frag {} falhou: {}", label, GL20.glGetShaderInfoLog(frag));
                GL20.glDeleteShader(vert);
                GL20.glDeleteShader(frag);
                return false;
            }

            int prog = GL20.glCreateProgram();
            GL20.glAttachShader(prog, vert);
            GL20.glAttachShader(prog, frag);
            GL20.glLinkProgram(prog);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);

            if (GL20.glGetProgrami(prog, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                NexusRenderHdrClient.LOGGER.debug("[NexusHDR] Link {} falhou: {}", label, GL20.glGetProgramInfoLog(prog));
                GL20.glDeleteProgram(prog);
                return false;
            }

            program = prog;
            quadVao = GL30.glGenVertexArrays();
            NexusRenderHdrClient.LOGGER.info("[NexusHDR] ACES shader compilado ({}) — programa {}.", label, program);
            return true;

        } catch (Exception e) {
            NexusRenderHdrClient.LOGGER.debug("[NexusHDR] Excecao ao compilar {}: {}", label, e.getMessage());
            return false;
        }
    }

    public static int getProgram() { return program; }
    public static int getQuadVao() { return quadVao; }
}