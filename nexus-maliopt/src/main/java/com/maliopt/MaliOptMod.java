package com.maliopt;

import com.maliopt.config.MaliOptConfig;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.gpu.MobileGluesDetector;
import com.maliopt.performance.PerformanceGuard;
import com.maliopt.pipeline.FBFetchBloomPass;
import com.maliopt.pipeline.MaliPipelineOptimizer;
import com.maliopt.pipeline.PLSLightingPass;
import com.maliopt.geometry.FrustumCuller;
import com.maliopt.geometry.OcclusionCuller;
import com.maliopt.geometry.GreedyMesher;
import com.maliopt.geometry.MultiDrawManager;
import com.maliopt.pipeline.ShadowPass;
import com.maliopt.pipeline.ColoredLightsPass;
import com.maliopt.pipeline.ShaderCacheManager;
import com.maliopt.shader.ShaderCache;
import com.maliopt.shader.ShaderCapabilities;
import com.maliopt.shader.ShaderExecutionLayer;
import com.nexuapicore.NexusAPI;
import com.nexuapicore.core.FeatureRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.opengl.GL11;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean gpuDetected = false;
    private String renderer = "";
    private String vendor = "";
    private String version = "";

    @Override
    public void onInitializeClient() {
        // Detectar GPU assim que o contexto GL estiver pronto
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            renderer = GL11.glGetString(GL11.GL_RENDERER);
            vendor = GL11.glGetString(GL11.GL_VENDOR);
            version = GL11.glGetString(GL11.GL_VERSION);
            LOGGER.info("[MaliOpt] Cliente iniciado — verificando GPU...");
            LOGGER.info("[MaliOpt] Renderer : " + renderer);
            LOGGER.info("[MaliOpt] Vendor   : " + vendor);
            LOGGER.info("[MaliOpt] Version  : " + version);
            MobileGluesDetector.detect();
            gpuDetected = true;
            LOGGER.info("[MaliOpt] ✅ GPU Mali detectada — activando optimizações");
        });

        // Aguardar pelo FeatureRegistry do Nexus API Core
        if (NexusAPI.isReady()) {
            applyMaliOpt(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyMaliOpt);
        }
    }

    private void applyMaliOpt(FeatureRegistry registry) {
        if (!gpuDetected) {
            renderer = GL11.glGetString(GL11.GL_RENDERER);
            vendor = GL11.glGetString(GL11.GL_VENDOR);
            version = GL11.glGetString(GL11.GL_VERSION);
            gpuDetected = true;
        }
        var caps = registry.getActiveCapabilities();
        LOGGER.info("[MaliOpt] Capabilities recebidas: " + caps);
        // Ativar otimizações baseadas nas capabilities (ex: PLS, tile-based, etc.)
    }
}
