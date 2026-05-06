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

    private static final float[] CASCADE_SPLITS = { 0.0f, 0.25f, 1.0f }; // near, mid, far

    private static int   locLightMVP;
    private static int   locModel;

    // ── GLSL VERTEX (depth-only) ────────────────────────────────────
    private static final String VERT =
        "#version 310 es\n" +
        "layout(location = 0) in vec3 aPos;\n" +
        "uniform mat4 uLightMVP;\n" +
        "uniform mat4 uModel;\n" +
        "void main() {\n" +
        "    gl_Position = uLightMVP * uModel * vec4(aPos, 1.0);\n" +
        "}\n";

    // ── GLSL FRAGMENT (depth-only, vazio) ──────────────────────────
    private static final String FRAG =
        "#version 310 es\n" +
        "void main() {}\n";

    public static void init() {
        if (ready) return;
        try {
            int vert = ShaderExecutionLayer.compile(GL20.GL_VERTEX_SHADER, VERT, "Shadow_vert");
            int frag = ShaderExecutionLayer.compile(GL20.GL_FRAGMENT_SHADER, FRAG, "Shadow_frag");
            if (vert == 0 || frag == 0) { ready = false; return; }
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vert);
            GL20.glAttachShader(program, frag);
            GL20.glLinkProgram(program);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                MaliOptMod.LOGGER.error("[MaliOpt] ShadowPass link falhou: {}", GL20.glGetProgramInfoLog(program));
                ready = false; return;
            }
            locLightMVP = GL20.glGetUniformLocation(program, "uLightMVP");
            locModel    = GL20.glGetUniformLocation(program, "uModel");
            rebuildAllCascades();
            ready = true;
            MaliOptMod.LOGGER.info("[MaliOpt] ✅ ShadowPass CSM ({} cascatas) iniciado", CASCADE_COUNT);
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[MaliOpt] ShadowPass.init() excepção: {}", e.getMessage());
            ready = false;
        }
    }

    public static void render(MinecraftClient mc) {
        if (!ready || mc.world == null || program == 0) return;

        // Guarda estado GL
        previousFbo     = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int[] prevVp    = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevVp);
        boolean depth   = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean cull    = GL11.glIsEnabled(GL11.GL_CULL_FACE);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_FRONT); // shadow acne mitigation
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL20.glUseProgram(program);

        // Para cada cascata, renderizar a cena do ponto de vista da luz
        for (int cascade = 0; cascade < CASCADE_COUNT; cascade++) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbos[cascade]);
            GL11.glViewport(0, 0, SHADOW_SIZE, SHADOW_SIZE);
            GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

            // MVP da luz (simplificado: sol direcional)
            Matrix4f lightView = new Matrix4f().lookAt(
                new Vector3f(50f, 80f, 50f),
                new Vector3f(0f, 0f, 0f),
                new Vector3f(0f, 1f, 0f)
            );
            Matrix4f lightProj = new Matrix4f().ortho(
                -80f, 80f, -80f, 80f, 0.1f, 160f
            );
            Matrix4f lightMVP = lightProj.mul(lightView);
            float[] mvpArr = new float[16];
            lightMVP.get(mvpArr);
            GL20.glUniformMatrix4fv(locLightMVP, false, mvpArr);

            // Modelo identidade (MVP faz tudo)
            GL20.glUniformMatrix4fv(locModel, false, new float[]{
                1,0,0,0, 0,1,0,0, 0,0,1,0, 0,0,0,1
            });

            // Minecraft renderiza os chunks dentro deste FBO (via Mixin)
            // O MixinGameRenderer já injeta hooks no render do WorldRenderer
            // (a ligação será feita pelo MaliOptMod via WorldRenderEvents)
        }

        // Restaura estado GL
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFbo);
        GL11.glViewport(prevVp[0], prevVp[1], prevVp[2], prevVp[3]);
        GL20.glUseProgram(prevProgram);
        if (!depth) GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (!cull)  GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glDepthFunc(GL11.GL_LESS);
    }

    // ── Métodos para o PLSLightingPass usar ──────────────────────
    public static int getShadowMap(int cascade) { return shadowTexs[cascade]; }
    public static float[] getCascadeSplits()   { return CASCADE_SPLITS; }
    public static int   getCascadeCount()      { return CASCADE_COUNT; }

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
        for (int i = 0; i < CASCADE_COUNT; i++) {
            if (shadowFbos[i] != 0) GL30.glDeleteFramebuffers(shadowFbos[i]);
            if (shadowTexs[i] != 0) GL11.glDeleteTextures(shadowTexs[i]);
        }
        ready = false;
    }
    public static boolean isReady() { return ready; }
}
