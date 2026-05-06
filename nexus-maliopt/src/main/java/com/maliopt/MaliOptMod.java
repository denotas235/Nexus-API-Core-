package com.maliopt;

import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.config.MaliOptConfig;
import com.maliopt.geometry.FrustumCuller;
import com.maliopt.geometry.GreedyMesher;
import com.maliopt.geometry.MultiDrawManager;
import com.maliopt.geometry.OcclusionCuller;
import com.maliopt.gpu.ExtensionActivator;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.gpu.MobileGluesDetector;
import com.maliopt.integration.IrisBridge;
import com.maliopt.pipeline.ColoredLightsPass;
import com.maliopt.pipeline.FBFetchBloomPass;
import com.maliopt.pipeline.MaliPipelineOptimizer;
import com.maliopt.pipeline.PLSLightingPass;
import com.maliopt.pipeline.ShadowPass;
import com.maliopt.pipeline.ShaderCacheManager;
import com.maliopt.shader.ShaderCache;
import com.maliopt.shader.ShaderCapabilities;
import com.maliopt.shader.ShaderExecutionLayer;
import com.nexuapicore.NexusAPI;
import com.nexuapicore.core.FeatureRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Registry guardado se chegar antes do GL estar pronto
    private static FeatureRegistry pendingRegistry = null;
    private static boolean glReady = false;

    @Override
    public void onInitializeClient() {

        // GL só está disponível a partir de CLIENT_STARTED
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            glReady = true;

            LOGGER.info("[MaliOpt] ══════ MaliOpt iniciando ══════");
            LOGGER.info("[MaliOpt] Renderer : {}", GPUDetector.getRenderer());
            LOGGER.info("[MaliOpt] Vendor   : {}", GPUDetector.getVendor());
            LOGGER.info("[MaliOpt] Version  : {}", GPUDetector.getVersion());

            MobileGluesDetector.detect();
            IrisBridge.init();

            if (!GPUDetector.isMaliGPU()) {
                LOGGER.warn("[MaliOpt] GPU não é Mali — mod inactivo");
                return;
            }

            LOGGER.info("[MaliOpt] ✅ GPU Mali detectada");

            // Se o registry já chegou antes do GL, aplica agora
            if (pendingRegistry != null) {
                applyMaliOpt(pendingRegistry);
                pendingRegistry = null;
            }
        });

        // Nexus API pode chamar onReady antes ou depois de CLIENT_STARTED
        if (NexusAPI.isReady()) {
            applyMaliOpt(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyMaliOpt);
        }
    }

    private void applyMaliOpt(FeatureRegistry registry) {
        if (!glReady) {
            // GL ainda não está pronto — guardar para CLIENT_STARTED
            pendingRegistry = registry;
            LOGGER.info("[MaliOpt] Registry recebido antes do GL — aguardando CLIENT_STARTED");
            return;
        }

        if (!GPUDetector.isMaliGPU()) return;

        // 1. Ler extensões do registry (fonte de verdade — sem forçar nada)
        ExtensionActivator.activateFromRegistry(registry);

        // 2. Capacidades dos shaders
        ShaderCapabilities.init(registry);

        // 3. ASTC — só se disponível no hardware
        if (ExtensionActivator.hasAstcLdr) {
            ASTCSubsystem.init();
        } else {
            LOGGER.info("[MaliOpt] ASTC não disponível — subsistema ignorado");
        }

        LOGGER.info("[MaliOpt] Capabilities activas: {}", registry.getActiveCapabilities());
        LOGGER.info("[MaliOpt] ══════ MaliOpt pronto ══════");
    }
}
