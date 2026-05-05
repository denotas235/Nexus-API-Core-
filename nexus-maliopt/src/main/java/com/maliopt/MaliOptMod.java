package com.maliopt;

import com.maliopt.config.MaliOptConfig;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.gpu.MobileGluesDetector;
import com.maliopt.performance.PerformanceGuard;
import com.maliopt.pipeline.FBFetchBloomPass;
import com.maliopt.pipeline.MaliPipelineOptimizer;
import com.maliopt.pipeline.PLSLightingPass;
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

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("[MaliOpt] Registando eventos...");
        MaliOptConfig.load();

        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {

            MobileGluesDetector.detect();

            LOGGER.info("[MaliOpt] Cliente iniciado — verificando GPU...");
            LOGGER.info("[MaliOpt] Renderer : {}", GPUDetector.getRenderer());
            LOGGER.info("[MaliOpt] Vendor   : {}", GPUDetector.getVendor());
            LOGGER.info("[MaliOpt] Version  : {}", GPUDetector.getVersion());

            if (MobileGluesDetector.isActive()) {
                LOGGER.info("[MaliOpt] GL Layer : MobileGlues v{} ✅",
                    formatMGVersion(MobileGluesDetector.mobileGluesVersion));
            } else {
                LOGGER.info("[MaliOpt] GL Layer : GL4ES (extensões Mali limitadas)");
            }

            if (GPUDetector.isMaliGPU()) {
                LOGGER.info("[MaliOpt] ✅ GPU Mali detectada — activando optimizações");

                // ── NOVO: Obter FeatureRegistry da Nexus API Core ─────
                FeatureRegistry registry = NexusAPI.featureRegistry;
                LOGGER.info("[MaliOpt] Nexus FeatureRegistry obtido: {} capacidades activas",
                    registry.getActiveCapabilities().size());

                // ── 1. Capacidades de shader via Nexus ─────────────────
                ShaderCapabilities.init(registry);

                // ── 2. Camada de execução de shaders ─────────────────
                ShaderExecutionLayer.init();

                // ── 3. Cache de shaders compilados ───────────────────
                try {
                    ShaderCache.init(FabricLoader.getInstance().getGameDir());
                } catch (Exception e) {
                    LOGGER.warn("[MaliOpt] ShaderCache.init falhou: {}", e.getMessage());
                }

                // ── 4. Pipeline de renderização ───────────────────────
                MaliPipelineOptimizer.init();
                ShaderCacheManager.init();

                // ── 5. Passes de post-processing ─────────────────────
                PLSLightingPass.init();
                FBFetchBloomPass.init();

            } else {
                LOGGER.info("[MaliOpt] ⚠️  GPU não é Mali — mod inactivo");
            }
        });

        // ── Post-process pipeline ─────────────────────────────────
        WorldRenderEvents.LAST.register(context -> {
            MinecraftClient mc = MinecraftClient.getInstance();

            PerformanceGuard.update(mc);

            if (PLSLightingPass.isReady() && PerformanceGuard.lightingPassEnabled()) {
                PLSLightingPass.render(mc);
            }

            if (FBFetchBloomPass.isReady() && PerformanceGuard.bloomEnabled()) {
                FBFetchBloomPass.render(mc);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            PLSLightingPass.cleanup();
            FBFetchBloomPass.cleanup();
        });
    }

    private static String formatMGVersion(int v) {
        if (v <= 0) return "desconhecida";
        return (v / 1000) + "." + ((v % 1000) / 100) + "." + (v % 100);
    }
}
