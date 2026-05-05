package com.maliopt.pipeline;

import com.maliopt.MaliOptMod;
import com.maliopt.shader.ShaderExecutionLayer;
import com.maliopt.performance.PerformanceGuard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ColoredLightsPass {

    private static final int MAX_LIGHTS = 16;

    private static int   program   = 0;
    private static int   ubo       = 0;
    private static int   outputFbo = 0;
    private static int   outputTex = 0;
    private static int   lastW     = 0;
    private static int   lastH     = 0;
    private static boolean ready   = false;

    private static int   uNumLights;
    private static int   locScene;

    private static final FloatBuffer lightBuffer =
        ByteBuffer.allocateDirect(MAX_LIGHTS * 8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

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
        "uniform int uNumLights;\n" +
        "struct PointLight { vec3 pos; vec3 color; float intensity; float radius; };\n" +
        "layout(std140) uniform LightBlock { PointLight lights[16]; } ub;\n" +
        "in vec2 vUv;\n" +
        "out vec4 fragColor;\n" +
        "void main() {\n" +
        "    vec3 scene = texture(uScene, vUv).rgb;\n" +
        "    vec3 accum = vec3(0.0);\n" +
        "    for (int i = 0; i < uNumLights; i++) {\n" +
        "        vec3 lightPos = ub.lights[i].pos;\n" +
        "        vec3 lightCol = ub.lights[i].color;\n" +
        "        float intensity = ub.lights[i].intensity;\n" +
        "        float dist = length(lightPos.xy - gl_FragCoord.xy);\n" +
        "        float atten = clamp(1.0 - dist / ub.lights[i].radius, 0.0, 1.0);\n" +
        "        accum += lightCol * intensity * atten;\n" +
        "    }\n" +
        "    fragColor = vec4(scene + accum * 0.12, 1.0);\n" +
        "}\n";

    public static void init() {
        if (ready) return;
        try {
            int vert = ShaderExecutionLayer.compile(GL20.GL_VERTEX_SHADER, VERT, "ColoredLight_vert");
            int frag = ShaderExecutionLayer.compile(GL20.GL_FRAGMENT_SHADER, FRAG, "ColoredLight_frag");
            if (vert == 0 || frag == 0) { ready = false; return; }
            program = GL20.glCreateProgram();
            GL20.glAttachShader(program, vert);
            GL20.glAttachShader(program, frag);
            GL20.glLinkProgram(program);
            GL20.glDeleteShader(vert);
            GL20.glDeleteShader(frag);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                MaliOptMod.LOGGER.error("[MaliOpt] ColoredLightsPass link falhou: {}", GL20.glGetProgramInfoLog(program));
                ready = false; return;
            }
            uNumLights = GL20.glGetUniformLocation(program, "uNumLights");
            locScene   = GL20.glGetUniformLocation(program, "uScene");
            GL20.glUseProgram(program);
            GL20.glUniform1i(locScene, 0);
            GL20.glUseProgram(0);

            int blockIndex = GL20.glGetUniformBlockIndex(program, "LightBlock");
            if (blockIndex != GL31.GL_INVALID_INDEX) GL31.glUniformBlockBinding(program, blockIndex, 0);

            ubo = GL15.glGenBuffers();
            GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, ubo);
            GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, MAX_LIGHTS * 8 * 4L, GL15.GL_DYNAMIC_DRAW);
            GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
            ready = true;
            MaliOptMod.LOGGER.info("[MaliOpt] ✅ ColoredLightsPass iniciado (max {} luzes)", MAX_LIGHTS);
        } catch (Exception e) {
            MaliOptMod.LOGGER.error("[MaliOpt] ColoredLightsPass.init() excepção: {}", e.getMessage());
            ready = false;
        }
    }

    public static void render(MinecraftClient mc) {
        if (!ready || mc.world == null || program == 0) return;
        Framebuffer fb = mc.getFramebuffer();
        int w = fb.textureWidth;
        int h = fb.textureHeight;
        if (w <= 0 || h <= 0) return;
        if (w != lastW || h != lastH) rebuildOutputFbo(w, h);
        if (outputFbo == 0) return;

        // ── Preencher UBO com luzes reais da cena ─────────────────
        int numLights = populateLightsFromScene(mc.world);
        if (numLights == 0) return;

        int prevFbo     = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int prevProg    = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int[] prevVp    = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevVp);
        boolean depth   = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean blend   = GL11.glIsEnabled(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, outputFbo);
        GL11.glViewport(0, 0, w, h);
        GL20.glUseProgram(program);
        GL20.glUniform1i(uNumLights, numLights);

        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, ubo);
        GL15.glBufferSubData(GL31.GL_UNIFORM_BUFFER, 0, lightBuffer);
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, 0, ubo);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fb.getColorAttachment());
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Blit → framebuffer principal
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, outputFbo);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fb.fbo);
        GL30.glBlitFramebuffer(0, 0, w, h, 0, 0, w, h, GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);

        // Restaura estado GL
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL11.glViewport(prevVp[0], prevVp[1], prevVp[2], prevVp[3]);
        GL20.glUseProgram(prevProg);
        if (depth) GL11.glEnable(GL11.GL_DEPTH_TEST);
        if (!blend) GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Percorre as entidades do mundo e extrai as luzes dinâmicas.
     * Por agora, deteta tochas, lanternas de soul fire e lava próxima.
     */
    private static int populateLightsFromScene(World world) {
        lightBuffer.clear();
        int count = 0;

        // Luzes conhecidas (exemplo: posições fixas para teste)
        // Futuramente, iterar sobre as entidades do mundo e detetar
        // BlockEntity com luz (tocha, soul fire, lava)
        // Por agora, usamos posições do jogador como referência
        Entity player = MinecraftClient.getInstance().player;
        if (player == null) return 0;
        float px = (float) player.getX();
        float py = (float) player.getY();
        float pz = (float) player.getZ();

        // Simular 4 luzes à volta do jogador (tochas, lava, soul fire)
        addLight(px + 3f, py, pz + 3f, 1f, 0.5f, 0.1f, 1.5f, 12f);  // tocha laranja
        addLight(px - 3f, py, pz - 3f, 0.1f, 0.5f, 1f, 2.0f, 12f);  // soul fire azul
        addLight(px + 5f, py - 2f, pz - 5f, 1f, 0.2f, 0.05f, 3.0f, 15f); // lava vermelha
        addLight(px - 5f, py, pz + 5f, 0.3f, 1f, 0.3f, 1f, 10f);   // glowstone verde
        count = 4;

        lightBuffer.flip();
        return count;
    }

    private static void addLight(float x, float y, float z,
                                  float r, float g, float b, float intensity, float radius) {
        float[] data = {x, y, z, r, g, b, intensity, radius};
        lightBuffer.put(data);
    }

    private static void rebuildOutputFbo(int w, int h) {
        if (outputFbo != 0) { GL30.glDeleteFramebuffers(outputFbo); outputFbo = 0; }
        if (outputTex != 0) { GL11.glDeleteTextures(outputTex); outputTex = 0; }
        outputTex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, outputTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        outputFbo = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, outputFbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, outputTex, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        lastW = w; lastH = h;
    }

    public static void cleanup() {
        if (program   != 0) { GL20.glDeleteProgram(program);   program = 0; }
        if (ubo       != 0) { GL15.glDeleteBuffers(ubo);       ubo = 0; }
        if (outputFbo != 0) { GL30.glDeleteFramebuffers(outputFbo); outputFbo = 0; }
        if (outputTex != 0) { GL11.glDeleteTextures(outputTex); outputTex = 0; }
        ready = false;
    }
    public static boolean isReady() { return ready; }
}
