package com.nexus.shadows;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.*;

public class ShadowPipeline {

    private static final int SHADOW_SIZE = 1024;
    private static final int GL_CLAMP_TO_BORDER = 0x812D;

    private static int shadowFbo = 0;
    private static int shadowTex = 0;
    private static boolean glReady    = false;
    private static float   sunAngleDeg = 0f;

    public static final Matrix4f lightSpaceMatrix = new Matrix4f();

    /** Chamado pelo GameRendererMixin no primeiro frame — GL disponivel. */
    public static void initGL() {
        if (glReady) return;

        try {
            shadowFbo = GL30.glGenFramebuffers();
            shadowTex = GL11.glGenTextures();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTex);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT16,
                    SHADOW_SIZE, SHADOW_SIZE, 0,
                    GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_SHORT, (java.nio.ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
            // Fora do shadow map = iluminado (sem sombra)
            GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD,
                    new float[]{1f, 1f, 1f, 1f});
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL11.GL_TEXTURE_2D, shadowTex, 0);
            GL11.glDrawBuffer(GL11.GL_NONE);
            GL11.glReadBuffer(GL11.GL_NONE);

            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                NexusShadowsClient.LOGGER.error("[Shadows] FBO incompleto (status={})", status);
                return;
            }

            ShadowMapShader.compile();
            ShadowPCFShader.compile();
            glReady = true;
            NexusShadowsClient.LOGGER.info("[Shadows] Pipeline GL pronto — FBO:{} Tex:{}", shadowFbo, shadowTex);

        } catch (Exception e) {
            NexusShadowsClient.LOGGER.error("[Shadows] Pipeline GL falhou: {}", e.getMessage());
        }
    }

    public static void updateLightMatrix(float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return;
        ClientWorld world = mc.world;

        float skyAngle = world.getSkyAngle(tickDelta);
        sunAngleDeg = skyAngle * 360f;
        float rad = skyAngle * 2f * (float) Math.PI;

        Vector3f sunDir = new Vector3f(
            (float) Math.cos(rad),
            (float) Math.sin(rad),
            0.2f
        ).normalize();

        Matrix4f proj = new Matrix4f().ortho(-20f, 20f, -20f, 20f, 0.1f, 100f);

        Vec3d cam = mc.gameRenderer.getCamera().getPos();
        Vector3f lightPos = new Vector3f((float)cam.x, (float)cam.y, (float)cam.z)
                .add(new Vector3f(sunDir).mul(30f));
        Vector3f target   = new Vector3f((float)cam.x, (float)cam.y, (float)cam.z);

        Matrix4f view = new Matrix4f().lookAt(lightPos, target, new Vector3f(0,1,0));
        proj.mul(view, lightSpaceMatrix);
    }

    public static void renderShadowPass(float tickDelta) {
        if (!glReady || shadowFbo == 0) return;
        updateLightMatrix(tickDelta);

        int prog = ShadowMapShader.getProgram();
        if (prog == 0) return;

        int prevFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int[] vp    = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowFbo);
        GL11.glViewport(0, 0, SHADOW_SIZE, SHADOW_SIZE);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL20.glUseProgram(prog);

        int loc = GL20.glGetUniformLocation(prog, "uLightSpaceMatrix");
        if (loc >= 0) {
            float[] buf = new float[16];
            lightSpaceMatrix.get(buf);
            GL20.glUniformMatrix4fv(loc, false, buf);
        }
        // Geometria renderizada pelo WorldRenderer no próximo draw call
        // (arquitetura completa na v1.1.0 — ver ROADMAP)
        GL20.glUseProgram(0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, prevFbo);
        GL11.glViewport(vp[0], vp[1], vp[2], vp[3]);
    }

    public static int     getShadowTexture() { return shadowTex; }
    public static boolean isReady()          { return glReady && shadowFbo != 0; }
    public static int     getSunAngleDeg()   { return (int) sunAngleDeg; }
}