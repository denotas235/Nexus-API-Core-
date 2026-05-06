package com.maliopt;

import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.config.MaliOptConfig;
import com.maliopt.gpu.ExtensionActivator;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.gpu.MobileGluesDetector;
import com.maliopt.performance.PerformanceGuard;
import com.maliopt.pipeline.*;
import com.maliopt.geometry.*;
import com.maliopt.shader.*;
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
import java.nio.file.Path;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean gpuDetected = false;
    private boolean optimizationsApplied = false;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            String vendor   = GL11.glGetString(GL11.GL_VENDOR);
            String version  = GL11.glGetString(GL11.GL_VERSION);
            LOGGER.info("[MaliOpt] Cliente iniciado — verificando GPU...");
            LOGGER.info("[MaliOpt] Renderer : {}", renderer);
            LOGGER.info("[MaliOpt] Vendor   : {}", vendor);
            LOGGER.info("[MaliOpt] Version  : {}", version);
            MobileGluesDetector.detect();
            gpuDetected = true;
            LOGGER.info("[MaliOpt] ✅ GPU Mali detectada — à espera das capabilities...");
            NexusAPI.registerModule(new com.nexus.modules.maliopt.MaliOptNexusModule());
        });

        if (NexusAPI.isReady()) {
            applyOptimizations(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyOptimizations);
        }

        WorldRenderEvents.END.register(ctx -> {
            if (!optimizationsApplied) return;
            MaliPipelineOptimizer.onFrameEnd();
        });
    }

    private void applyOptimizations(FeatureRegistry registry) {
        if (optimizationsApplied) return;
        optimizationsApplied = true;

        var caps = registry.getActiveCapabilities();
        LOGGER.info("[MaliOpt] Capabilities recebidas: {}", caps);

        ExtensionActivator.activateFromRegistry(registry);

        TileBasedOptimizer.init();
        MaliPipelineOptimizer.init();

        Path shaderCachePath = FabricLoader.getInstance().getGameDir().resolve("shader_cache");
        ShaderCache.init(shaderCachePath);

        // Inicializar ShaderCapabilities com o registry antes de qualquer shader
        ShaderCapabilities.init(registry);
        ShaderExecutionLayer.init();

        PLSLightingPass.init();
        FBFetchBloomPass.init();

        // ASTC desativado temporariamente: libastc_bridge_64.so requer libEGL.so.1 (desktop)
        // Para ativar, compilar uma versão Android da lib ou usar texturas ASTC offline.
        // ASTCSubsystem.init(); 
        LOGGER.info("[MaliOpt] ASTC desativado (lib nativa incompatível). Plano B: texturas pré-comprimidas.");

        LOGGER.info("[MaliOpt] ✅ Optimizações aplicadas com sucesso.");
    }
}
