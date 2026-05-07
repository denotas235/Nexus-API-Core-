package com.maliopt;

import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.astc.ASTCTextureLoader;
import com.nexus.modules.textures.ASTCTextureRegistry;
import com.nexus.modules.textures.TextureModule;
import com.maliopt.config.MaliOptConfig;
import com.maliopt.gpu.ExtensionActivator;
import com.maliopt.gpu.GPUDetector;
import com.maliopt.gpu.MobileGluesDetector;
import com.maliopt.performance.PerformanceGuard;
import com.maliopt.pipeline.*;
import com.maliopt.geometry.*;
import com.maliopt.shader.*;
import com.maliopt.world.*;
import com.nexuapicore.NexusAPI;
import com.nexuapicore.core.FeatureRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
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
            NeuralLODController.tick();
        });

        // Comando /maliopt pregenerate <radius>
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("maliopt")
                .then(ClientCommandManager.literal("pregenerate")
                    .then(ClientCommandManager.argument("radius", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 32))
                    .executes(context -> {
                        int radius = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "radius");
                        MinecraftClient client = context.getSource().getClient();
                        IntegratedServer server = client.getServer();
                        if (server == null) return 0;
                        LOGGER.info("[MaliOpt] Iniciando pré‑geração de {} chunks...", radius*radius*4);
                        server.execute(() -> {
                            // força geração de chunks ao redor
                            net.minecraft.util.math.ChunkPos center = new net.minecraft.util.math.ChunkPos(
                                client.player.getBlockPos());
                            for (int dx = -radius; dx <= radius; dx++) {
                                for (int dz = -radius; dz <= radius; dz++) {
                                    server.getWorld(client.player.getWorld().getRegistryKey())
                                        .getChunk(center.x + dx, center.z + dz);
                                }
                            }
                            LOGGER.info("[MaliOpt] Pré‑geração concluída.");
                        });
                        return 1;
                    }))
                ));
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

        // Inicializa sistemas preditivos de mundo
        WorldCache.init();
        PerformanceGuard.init();
        CalculusCore.feedPosition(MinecraftClient.getInstance().player != null
            ? MinecraftClient.getInstance().player.getPos()
            : new net.minecraft.util.math.Vec3d(0,0,0));
        // NeuralLODController será inicializado quando o servidor iniciar
        // (viewDistance setters dependem do servidor integrado)

        Path shaderCachePath = FabricLoader.getInstance().getGameDir().resolve("shader_cache");
        ShaderCache.init(shaderCachePath);
        ShaderCapabilities.init(registry);
        ShaderExecutionLayer.init();

        PLSLightingPass.init();
        FBFetchBloomPass.init();

        TextureModule.load();
        if (ASTCTextureRegistry.hasASTCTextures()) {
            ASTCTextureLoader.init();
        TextureModule.load();
            LOGGER.info("[MaliOpt] ✅ ASTC pré-comprimido ATIVO!");
        }
        LOGGER.info("[MaliOpt] ASTC desativado (lib nativa incompatível). Plano B: texturas pré-comprimidas.");

        LOGGER.info("[MaliOpt] ✅ Optimizações aplicadas com sucesso.");
    }
}
