package com.maliopt.mixin;

import com.maliopt.world.NexusBlockExtractor;
import com.maliopt.world.NexusVBOInjector;
import com.maliopt.world.VulkanWorker;
import com.maliopt.world.VulkanResult;

import net.minecraft.client.render.chunk.SectionBuilderTask;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionBuilderTask.class)
public abstract class MixinSectionBuilderTask {

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void nexus_interceptRun(CallbackInfo ci) {
        ci.cancel();

        SectionBuilderTask task = (SectionBuilderTask)(Object)this;
        ChunkBuilder.BuiltChunk chunk = task.chunk;
        ChunkRendererRegion region = task.createRegion();

        var blocks = NexusBlockExtractor.extract(region);

        VulkanResult result = VulkanWorker.submitChunk(blocks);

        NexusVBOInjector.inject(chunk, result);

        chunk.setNeedsRebuild(false);
    }
}
