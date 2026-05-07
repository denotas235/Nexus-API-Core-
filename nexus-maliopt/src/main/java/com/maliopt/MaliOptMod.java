package com.maliopt;

import com.maliopt.astc.ASTCSubsystem;
import com.maliopt.astc.ASTCTextureLoader;
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
import com.nexus.modules.textures.ASTCTextureRegistry;
import com.nexus.modules.textures.TextureModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.server.integrated.IntegratedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lwjgl.opengl.GL11;
import java.nio.file.Path;

public class MaliOptMod implements ClientModInitializer {

    public static final String MOD_ID = "maliopt";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private boolean optimizationsApplied = false;
    private GameOptions gameOptions;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            String renderer = GL11.glGetString(GL11.GL_RENDERER);
            String vendor   = GL11.glGetString(GL11.GL_VENDOR);
            String version  = GL11.glGetString(GL11.GL_VERSION);
            LOGGER.info("[MaliOpt] GPU: {} | {} | {}", renderer, vendor, version);
            MobileGluesDetector.detect();
            NexusAPI.registerModule(new com.nexus.modules.maliopt.MaliOptNexusModule());
        });

        if (NexusAPI.isReady()) {
            applyOptimizations(NexusAPI.getRegistry());
        } else {
            NexusAPI.onReady(this::applyOptimizations);
        }

        WorldRenderEvents.END.register(ctx -> {
            if (!optimizationsApplied) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.world == null) return;

            // Medir FPS e stress
            PerformanceGuard.onFrameEnd();

            // Efeitos visuais (PLS e Bloom)
            if (PLSLightingPass.isReady()) PLSLightingPass.render(mc);
            if (FBFetchBloomPass.isReady()) FBFetchBloomPass.render(mc);

            // Otimizações de frame
            MaliPipelineOptimizer.onFrameEnd();
            TileBasedOptimizer.onFrameEnd();

            // Ajuste dinâmico de LOD e qualidade
            NeuralLODController.tick();
            applyQualityScaler(mc);
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
                        LOGGER.info("[MaliOpt] Pré-geração de {} chunks...", radius*radius*4);
                        server.execute(() -> {
                            net.minecraft.util.math.ChunkPos center = new net.minecraft.util.math.ChunkPos(
                                client.player.getBlockPos());
                            for (int dx = -radius; dx <= radius; dx++) {
                                for (int dz = -radius; dz <= radius; dz++) {
                                    server.getWorld(client.player.getWorld().getRegistryKey())
                                        .getChunk(center.x + dx, center.z + dz);
                                }
                            }
                            LOGGER.info("[MaliOpt] Pré-geração concluída.");
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
        LOGGER.info("[MaliOpt] Capabilities: {}", caps.size());

        ExtensionActivator.activateFromRegistry(registry);

        TileBasedOptimizer.init();
        MaliPipelineOptimizer.init();
        WorldCache.init();
        PerformanceGuard.init();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            gameOptions = mc.options;
            NeuralLODController.init(gameOptions);
            CalculusCore.feedPosition(mc.player != null ? mc.player.getPos() : new net.minecraft.util.math.Vec3d(0,0,0));
        }

        Path shaderCachePath = FabricLoader.getInstance().getGameDir().resolve("shader_cache");
        ShaderCache.init(shaderCachePath);
        ShaderCapabilities.init(registry);
        ShaderExecutionLayer.init();

        PLSLightingPass.init();
        FBFetchBloomPass.init();

        TextureModule.load();
        if (ASTCTextureRegistry.hasASTCTextures()) {
            ASTCTextureLoader.init();
            LOGGER.info("[MaliOpt] ✅ ASTC ATIVO");
        } else {
            LOGGER.info("[MaliOpt] ASTC desativado (sem texturas no JAR)");
        }

        LOGGER.info("[MaliOpt] ✅ Otimizações aplicadas — 60 FPS lock ativo.");
    }

    // Escalonador de qualidade agressivo
    private void applyQualityScaler(MinecraftClient mc) {
        if (mc == null || gameOptions == null) return;
        PerformanceGuard.StressLevel stress = PerformanceGuard.getStressLevel();

        // Partículas
        if (stress == PerformanceGuard.StressLevel.CRITICAL) {
            if (gameOptions.getParticles().getValue() != ParticlesMode.MINIMAL) {
                gameOptions.getParticles().setValue(ParticlesMode.MINIMAL);
                LOGGER.warn("[MaliOpt] Partículas reduzidas ao mínimo (CRITICAL)");
            }
        } else if (stress == PerformanceGuard.StressLevel.HIGH) {
            if (gameOptions.getParticles().getValue() != ParticlesMode.DECREASED) {
                gameOptions.getParticles().setValue(ParticlesMode.DECREASED);
                LOGGER.info("[MaliOpt] Partículas diminuídas (HIGH)");
            }
        } else if (stress == PerformanceGuard.StressLevel.LOW) {
            if (gameOptions.getParticles().getValue() != ParticlesMode.ALL) {
                gameOptions.getParticles().setValue(ParticlesMode.ALL);
            }
        }

        // Distância de simulação (server side) — usa o mesmo valor da view?
        // Já controlado pelo NeuralLOD

        // Outras otimizações possíveis: reduzir a renderização de nuvens, etc.
        // Exemplo: gameOptions.enableClouds = (stress != StressLevel.CRITICAL);
    }
}
