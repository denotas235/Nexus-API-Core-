package com.maliopt;

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
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.opengl.GL11;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean gpuDetected = false;
    private boolean optimizationsApplied = false;

    @Override
    public void onInitializeClient() {
        // 1. Detectar GPU assim que o contexto GL existir
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
            // Registar o módulo no Nexus (pode ser pending se ainda não tiver arrancado)
            NexusAPI.registerModule(new com.nexus.modules.maliopt.MaliOptNexusModule());
        });

        // 2. Assim que o registry estiver pronto, aplicar optimizações
        if (NexusAPI.isReady()) {
            applyOptimizations(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyOptimizations);
        }

        // 3. Hook de render seguro (só arranca depois de tudo pronto)
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

        // Activar as flags do MaliOpt a partir do registry
        ExtensionActivator.activateFromRegistry(registry);

        // Agora que as flags estão corretas, inicializar os componentes
        TileBasedOptimizer.init();          // TBDR, PLS, FB fetch
        MaliPipelineOptimizer.init();       // pipeline global
        ShaderCache.init();
        ShaderExecutionLayer.init();
        // Inicialização dos passes de render (são seguros porque verificam as capabilities)
        PLSLightingPass.init();
        FBFetchBloomPass.init();

        // ASTC subsystem (vai usar EXT_texture_compression_astc_decode_mode e afins)
        ASTCSubsystem.init();

        LOGGER.info("[MaliOpt] ✅ Optimizações aplicadas com sucesso.");
    }
}
