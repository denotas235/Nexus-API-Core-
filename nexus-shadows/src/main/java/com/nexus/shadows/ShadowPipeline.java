package com.nexus.shadows;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

public class ShadowPipeline {
    private static int shadowFbo = 0;
    private static int shadowTex = 0;
    private static final int SHADOW_WIDTH  = 1024;
    private static final int SHADOW_HEIGHT = 1024;
    private static boolean initialized = false;

    // Matrizes para o shadow mapping
    public static final Matrix4f lightSpaceMatrix = new Matrix4f();

    public static void init() {
        if (initialized) return;
        initialized = true;

        shadowFbo = GL30.glGenFramebuffers();
        shadowTex = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTex);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT,
                SHADOW_WIDTH, SHADOW_HEIGHT, 0,
                GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, (java.nio.ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 0x812F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 0x812F);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                GL11.GL_TEXTURE_2D, shadowTex, 0);
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        ShadowMapShader.compile();
        ShadowPCFShader.compile();
        System.out.println("[Shadows] Shadow pipeline initialized.");
    }

    /**
     * Calcula a matriz de luz com base no ângulo do sol.
     */
    public static void updateLightMatrix(float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;
        ClientWorld world = mc.world;

        // Ângulo do sol (0.0 = meio‑dia, 0.5 = meia‑noite)
        float skyAngle = world.getSkyAngle(tickDelta);
        float angleRad = skyAngle * 2.0f * (float) Math.PI;

        // Vetor da direção do sol
        Vector3f sunDir = new Vector3f(
            (float) Math.cos(angleRad),
            (float) Math.sin(angleRad),
            0.2f
        );

        // Projeção ortográfica (área de 40×40 blocos ao redor do jogador)
        Matrix4f lightProjection = new Matrix4f();
        lightProjection.setOrtho(-20.0f, 20.0f, -20.0f, 20.0f, 0.1f, 100.0f);

        // View Matrix: posição da câmara a olhar para o centro
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();
        Vector3f lightPos = new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z)
                .add(sunDir.mul(30.0f));
        Vector3f target = new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);

        Matrix4f lightView = new Matrix4f();
        lightView.setLookAt(lightPos, target, up);

        // Matriz final: espaço de luz = Proj * View
        lightProjection.mul(lightView, lightSpaceMatrix);
    }

    /**
     * Renderiza o shadow map. Chamado ANTES do mundo.
     */
    public static void renderShadowPass(float tickDelta) {
        if (shadowFbo == 0) return;
        updateLightMatrix(tickDelta);

        int prog = ShadowMapShader.getProgram();
        if (prog == 0) return;

        int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int[] prevViewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, prevViewport);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
        GL11.glViewport(0, 0, SHADOW_WIDTH, SHADOW_HEIGHT);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(prog);

        // Passa a matriz de luz ao shader
        int loc = GL20.glGetUniformLocation(prog, "uLightSpaceMatrix");
        if (loc >= 0) {
            float[] buf = new float[16];
            lightSpaceMatrix.get(buf);
            GL20.glUniformMatrix4fv(loc, false, buf);
        }

        // O Minecraft desenhará a cena após este pass; apenas configuramos o FBO e o shader.
        GL20.glUseProgram(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL11.glViewport(prevViewport[0], prevViewport[1], prevViewport[2], prevViewport[3]);
    }

    public static int getShadowTexture() { return shadowTex; }
    public static boolean isReady() { return initialized && shadowFbo != 0 && ShadowPCFShader.getProgram() != 0; }
}
