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
import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.shader.ShaderExecutionLayer;
import com.nexuapicore.NexusAPI;
import com.nexuapicore.core.FeatureRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // FeatureRegistry guardado para usar quando GL estiver pronto
    private static FeatureRegistry pendingRegistry = null;
    private static boolean glReady = false;

    @Override
    public void onInitializeClient() {
        // Contexto GL só está disponível a partir daqui
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            glReady = true;
            LOGGER.info("[MaliOpt] Cliente iniciado — verificando GPU...");
            LOGGER.info("[MaliOpt] Renderer : {}", GPUDetector.getRenderer());
            LOGGER.info("[MaliOpt] Vendor   : {}", GPUDetector.getVendor());
            LOGGER.info("[MaliOpt] Version  : {}", GPUDetector.getVersion());
            MobileGluesDetector.detect();

            if (GPUDetector.isMaliGPU()) {
                LOGGER.info("[MaliOpt] ✅ GPU Mali detectada — activando optimizações");
                // Se o registry já chegou antes do GL estar pronto, aplica agora
                if (pendingRegistry != null) {
                    applyMaliOpt(pendingRegistry);
                    pendingRegistry = null;
                }
            } else {
                LOGGER.warn("[MaliOpt] GPU não é Mali — optimizações desactivadas");
            }
        });

        // Nexus API pode chamar onReady antes ou depois do CLIENT_STARTED
        if (NexusAPI.isReady()) {
            applyMaliOpt(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyMaliOpt);
        }
    }

    private void applyMaliOpt(FeatureRegistry registry) {
        if (!glReady) {
            // GL ainda não está pronto — guardar para aplicar no CLIENT_STARTED
            pendingRegistry = registry;
            LOGGER.info("[MaliOpt] Registry recebido antes do GL — aguardando CLIENT_STARTED");
            return;
        }
        if (!GPUDetector.isMaliGPU()) return;

        ShaderCapabilities.init(registry);
        if (ShaderCapabilities.ASTC) ASTCSubsystem.init();
        var caps = registry.getActiveCapabilities();
        LOGGER.info("[MaliOpt] Capabilities recebidas: {}", caps);
    }
}
