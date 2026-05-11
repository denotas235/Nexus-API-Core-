package com.maliopt.pipeline;

import com.maliopt.MaliOptMod;
import com.maliopt.shader.ShaderExecutionLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class ShadowPass {

    private static final int CASCADE_COUNT = 2;
    private static final int SHADOW_SIZE   = 1024;

    private static int   program         = 0;
    private static int[] shadowFbos      = new int[CASCADE_COUNT];
    private static int[] shadowTexs      = new int[CASCADE_COUNT];
    private static int   previousFbo     = 0;
    private static boolean ready         = false;
    private static boolean glInitDone    = false;

    private static final float[] CASCADE_SPLITS = { 0.0f, 0.25f, 1.0f };

    private static int locLightMVP;
    private static int locModel;

    // Compositing shader (PCF fullscreen quad)
    private static int  pcfProgram  = 0;
    private static int  pcfQuadVao  = 0;
    private static final String VERT_PCF =
        "#version 310 es\n" +
        "out vec2 vUv;\n" +
        "void main() {\n" +
        "    vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);\n" +
        "    vUv = uv;\n" +
        "    gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);\n" +
        "}\n";
    private static final String FRAG_PCF =
        "#version 310 es\n" +
        "precision mediump float;\n" +
        "uniform sampler2D uScene;\n" +
        "uniform sampler2D uShadowMap;\n" +
        "in  vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "float pcf(sampler2D sm, vec2 uv) {\n" +
        "    float s = 0.0; vec2 tx = 1.0/vec2(1024.0);\n" +
        "    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++)\n" +
        "        s += texture(sm, uv + vec2(float(x),float(y))*tx).r < 0.999 ? 0.5 : 0.0;\n" +
        "    return s / 9.0;\n" +
        "}\n" +
        "void main() {\n" +
        "    vec3 c = texture(uScene, vUv).rgb;\n" +
        "    float sh = pcf(uShadowMap, vUv);\n" +
        "    fragColor = vec4(c * (1.0 - sh * 0.55), 1.0);\n" +
        "}\n";

    private static final String VERT_DEPTH =
        "#version 310 es\n" +
        "layout(location = 0) in vec3 aPos;\n" +
        "uniform mat4 uLightMVP;\n" +
        "uniform mat4 uModel;\n" +
        "void main() {\n" +
        "    gl_Position = uLightMVP * uModel * vec4(aPos, 1.0);\n" +
        "}\n";

    private static final String FRAG_DEPTH =
        "#version 310 es\n" +
        "void main() {}\n";

    /** Deferred GL init — deve ser chamado dentro do contexto GL (primeiro frame de render). */
    public static void init() {
        if (glInitDone) return;
        glInitDone = true;
        try {
            // Depth shader
            int vert = ShaderExecutionLayer.compile(GL20.GL_VERTEX_SHADER, VERT_DEPTH, "Shadow_vert");
            int frag = ShaderExecutionLayer.compile(GL20.GL_FRAGMENT_SHADER, FRAG_DEPTH, "Shadow_frag");
            if (vert == 0 || frag == 0) { return; }
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vert);
            GL20.glAttachShader(program, frag);
            GL20.glLinkProgram(program);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                MaliOptMod.LOGGER.error("[ShadowPass] depth link falhou: {}", GL20.glGetProgramInfoLog(program));
                return;
            }
            locLightMVP = GL20.glGetUniformLocation(program, "uLightMVP");
            locModel    = GL20.glGetUniformLocation(program, "uModel");
            rebuildAllCascades();

            // PCF compositing shader
            int pv = ShaderExecutionLayer.compile(GL20.GL_VERTEX_SHADER, VERT_PCF, "ShadowPCF_vert");
            int pf = ShaderExecutionLayer.compile(GL20.GL_FRAGMENT_SHADER, FRAG_PCF, "ShadowPCF_frag");
            if (pv != 0 && pf != 0) {
                pcfProgram = GL20.glCreateProgram();
                GL20.glAttachShader(pcfProgram, pv);
                GL20.glAttachShader(pcfProgram, pf);
                GL20.glLinkProgram(pcfProgram);
                GL20.glDeleteShader(pv);
                GL20.glDeleteShader(pf);
                if (GL20.glGetProgrami(pcfProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                    MaliOptMod.LOGGER.warn("[ShadowPass] PCF link falhou — compositing desativado.");
                    GL20.glDeleteProgram(pcfProgram);
                    pcfProgram = 0;
                } else {
                    pcfQuadVao = GL30.glGenVertexArrays();
                }
            } else {
                GL20.glDeleteShader(pv);
                GL20.glDeleteShader(pf);
            }

            ready = true;
            MaliOptMod.LOGGER.info("[ShadowPass] Pronto — CSM:{} cascatas, PCF:{}", CASCADE_COUNT, pcfProgram != 0);
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[ShadowPass] init() excecao: {}", e.getMessage());
        }
    }

    public static void render(MinecraftClient mc) {
        if (!ready || mc.world == null || program == 0) return;

        previousFbo     = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int[] prevVp    = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevVp);
        boolean depth   = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull    = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL20.glUseProgram(program);

        for (int cascade = 0; cascade < CASCADE_COUNT; cascade++) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbos[cascade]);
            GL11.glViewport(0, 0, SHADOW_SIZE, SHADOW_SIZE);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

            Matrix4f lightView = new Matrix4f().lookAt(
                new Vector3f(50f, 80f, 50f), new Vector3f(0f, 0f, 0f), new Vector3f(0f, 1f, 0f));
            Matrix4f lightProj = new Matrix4f().ortho(-80f, 80f, -80f, 80f, 0.1f, 160f);
            Matrix4f lightMVP  = lightProj.mul(lightView);
            float[] mvpArr = new float[16];
            lightMVP.get(mvpArr);
            GL20.glUniformMatrix4fv(locLightMVP, false, mvpArr);
            GL20.glUniformMatrix4fv(locModel, false, new float[]{1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1});
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFbo);
        GL11.glViewport(prevVp[0], prevVp[1], prevVp[2], prevVp[3]);
        GL20.glUseProgram(prevProgram);
        if (!depth) GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (!cull)  GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glDepthFunc(GL11.GL_LESS);
    }

    /** Aplica o shadow map como post-process sobre o framebuffer do Minecraft.
     *  Deve ser chamado em WorldRenderEvents.END ou WorldRendererMixin TAIL. */
    public static void applyToScreen() {
        if (!ready || pcfProgram == 0 || shadowTexs[0] == 0) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null || mc.getFramebuffer() == null) return;

        int fbo = mc.getFramebuffer().fbo;
        int w   = mc.getWindow().getFramebufferWidth();
        int h   = mc.getWindow().getFramebufferHeight();

        int prevFbo  = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProg = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
        GL11.glViewport(0, 0, w, h);
        GL20.glUseProgram(pcfProgram);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.getFramebuffer().getColorAttachment());
        GL20.glUniform1i(GL20.glGetUniformLocation(pcfProgram, "uScene"), 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTexs[0]);
        GL20.glUniform1i(GL20.glGetUniformLocation(pcfProgram, "uShadowMap"), 1);

        GL30.glBindVertexArray(pcfQuadVao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL30.glBindVertexArray(0);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL20.glUseProgram(prevProg);
    }

    public static int getShadowMap(int cascade) { return cascade < CASCADE_COUNT ? shadowTexs[cascade] : 0; }
    public static float[] getCascadeSplits()    { return CASCADE_SPLITS; }
    public static int getCascadeCount()         { return CASCADE_COUNT; }
    public static boolean isReady()             { return ready; }

    private static void rebuildAllCascades() {
        for (int i = 0; i < CASCADE_COUNT; i++) {
            if (shadowTexs[i] != 0) GL11.glDeleteTextures(shadowTexs[i]);
            if (shadowFbos[i] != 0) GL30.glDeleteFramebuffers(shadowFbos[i]);

            shadowTexs[i] = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTexs[i]);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_DEPTH_COMPONENT16,
                SHADOW_SIZE, SHADOW_SIZE, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, 0L);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            shadowFbos[i] = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbos[i]);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D, shadowTexs[i], 0);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }
    }

    public static void cleanup() {
        if (program != 0) { GL20.glDeleteProgram(program); program = 0; }
        if (pcfProgram != 0) { GL20.glDeleteProgram(pcfProgram); pcfProgram = 0; }
        for (int i = 0; i < CASCADE_COUNT; i++) {
            if (shadowFbos[i] != 0) GL30.glDeleteFramebuffers(shadowFbos[i]);
            if (shadowTexs[i] != 0) GL11.glDeleteTextures(shadowTexs[i]);
        }
        ready = false;
        glInitDone = false;
    }
}

