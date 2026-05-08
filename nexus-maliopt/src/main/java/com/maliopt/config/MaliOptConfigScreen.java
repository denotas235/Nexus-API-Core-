package com.maliopt.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class MaliOptConfigScreen implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("MaliOpt Configuration"));

        ConfigEntryBuilder entry = builder.entryBuilder();

        // ── TAB 1: PERFORMANCE ─────────────────────────────────────
        ConfigCategory perf = builder.getOrCreateCategory(Text.literal("⚡ Performance"));
        perf.addEntry(entry.startIntSlider(Text.literal("FPS Target"), MaliOptConfig.fpsTarget, 30, 120)
            .setDefaultValue(60).setSaveConsumer(v -> MaliOptConfig.fpsTarget = v).build());
        perf.addEntry(entry.startIntSlider(Text.literal("Render Distance"), MaliOptConfig.renderDistance, 4, 16)
            .setDefaultValue(8).setSaveConsumer(v -> MaliOptConfig.renderDistance = v).build());
        perf.addEntry(entry.startBooleanToggle(Text.literal("Auto View Distance"), MaliOptConfig.autoViewDistance)
            .setDefaultValue(false).setSaveConsumer(v -> MaliOptConfig.autoViewDistance = v).build());
        perf.addEntry(entry.startBooleanToggle(Text.literal("Elytra Mode"), MaliOptConfig.elytraMode)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.elytraMode = v).build());
        perf.addEntry(entry.startBooleanToggle(Text.literal("Greedy Meshing"), MaliOptConfig.greedyMeshing)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.greedyMeshing = v).build());
        perf.addEntry(entry.startIntSlider(Text.literal("Greedy Max Cube Size"), MaliOptConfig.greedyMaxSize, 8, 32)
            .setDefaultValue(16).setSaveConsumer(v -> MaliOptConfig.greedyMaxSize = v).build());

        // ── TAB 2: VISUAL ──────────────────────────────────────────
        ConfigCategory visual = builder.getOrCreateCategory(Text.literal("🎨 Visual"));
        visual.addEntry(entry.startBooleanToggle(Text.literal("PLS Lighting"), MaliOptConfig.plsEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.plsEnabled = v).build());
        visual.addEntry(entry.startFloatField(Text.literal("Warmth"), MaliOptConfig.warmth)
            .setMin(0f).setMax(1f).setDefaultValue(0.3f).setSaveConsumer(v -> MaliOptConfig.warmth = v).build());
        visual.addEntry(entry.startFloatField(Text.literal("Ambient Occlusion"), MaliOptConfig.ambientOcclusion)
            .setMin(0f).setMax(1f).setDefaultValue(0.6f).setSaveConsumer(v -> MaliOptConfig.ambientOcclusion = v).build());
        visual.addEntry(entry.startBooleanToggle(Text.literal("Bloom"), MaliOptConfig.bloomEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.bloomEnabled = v).build());
        visual.addEntry(entry.startFloatField(Text.literal("Bloom Threshold"), MaliOptConfig.bloomThreshold)
            .setMin(0f).setMax(1f).setDefaultValue(0.3f).setSaveConsumer(v -> MaliOptConfig.bloomThreshold = v).build());
        visual.addEntry(entry.startFloatField(Text.literal("Bloom Radius"), MaliOptConfig.bloomRadius)
            .setMin(0.5f).setMax(3f).setDefaultValue(1.5f).setSaveConsumer(v -> MaliOptConfig.bloomRadius = v).build());
        visual.addEntry(entry.startFloatField(Text.literal("Bloom Intensity"), MaliOptConfig.bloomIntensity)
            .setMin(0.1f).setMax(2f).setDefaultValue(0.7f).setSaveConsumer(v -> MaliOptConfig.bloomIntensity = v).build());
        visual.addEntry(entry.startBooleanToggle(Text.literal("Shadows"), MaliOptConfig.shadowsEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.shadowsEnabled = v).build());
        visual.addEntry(entry.startBooleanToggle(Text.literal("ASTC Textures"), MaliOptConfig.astcEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.astcEnabled = v).build());
        visual.addEntry(entry.startBooleanToggle(Text.literal("sRGB"), MaliOptConfig.sRGB)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.sRGB = v).build());
        visual.addEntry(entry.startBooleanToggle(Text.literal("Fog"), MaliOptConfig.fogEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.fogEnabled = v).build());

        // ── TAB 3: ADVANCED ────────────────────────────────────────
        ConfigCategory advanced = builder.getOrCreateCategory(Text.literal("🔧 Advanced"));
        advanced.addEntry(entry.startBooleanToggle(Text.literal("glInvalidateFramebuffer"), MaliOptConfig.glInvalidateFramebuffer)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.glInvalidateFramebuffer = v).build());
        advanced.addEntry(entry.startBooleanToggle(Text.literal("Binary Shader Cache"), MaliOptConfig.binaryShaderCache)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.binaryShaderCache = v).build());
        advanced.addEntry(entry.startBooleanToggle(Text.literal("ARM Program Binary"), MaliOptConfig.armProgramBinary)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.armProgramBinary = v).build());
        advanced.addEntry(entry.startBooleanToggle(Text.literal("mediump Optimisation"), MaliOptConfig.mediumpOptimisation)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.mediumpOptimisation = v).build());
        advanced.addEntry(entry.startBooleanToggle(Text.literal("HUD (debug)"), MaliOptConfig.hudEnabled)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.hudEnabled = v).build());
        advanced.addEntry(entry.startBooleanToggle(Text.literal("Capability Report on Start"), MaliOptConfig.capabilityReportOnStart)
            .setDefaultValue(true).setSaveConsumer(v -> MaliOptConfig.capabilityReportOnStart = v).build());

        builder.setSavingRunnable(() -> {
            System.out.println("MaliOpt config saved.");
        });
        return builder.build();
    }
}
