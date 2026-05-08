package com.maliopt.mixin;

import com.maliopt.world.NexusBlockExtractor;
import com.maliopt.world.VulkanWorker;
import com.maliopt.world.VulkanResult;
import com.maliopt.world.NexusVBOInjector;

import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.client.render.VertexSorter;
import net.minecraft.client.render.chunk.BlockBufferBuilderPool;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionBuilder.class)
public abstract class MixinSectionBuilder {

    @Inject(method = "build", at = @At("HEAD"), cancellable = true)
    private void nexus_interceptBuild(
        ChunkSectionPos sectionPos,
        ChunkRendererRegion region,
        VertexSorter sorter,
        BlockBufferBuilderPool pool,
        CallbackInfoReturnable<SectionBuilder.RenderData> cir) {

        var blocks = NexusBlockExtractor.extract(region);

        VulkanResult result = VulkanWorker.submitChunk(blocks);

        int ox = region.getOriginX();
        int oy = region.getOriginY();
        int oz = region.getOriginZ();

        cir.setReturnValue(NexusVBOInjector.createRenderData(result, ox, oy, oz));
    }
}
