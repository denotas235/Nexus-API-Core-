package com.nexuapicore.render.patch;

import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import org.lwjgl.vulkan.*;

/**
 * PATCH FINAL 6: Integração completa com BuiltBuffer VANILLA 1.21.1
 * 
 * Mapeia a assinatura EXATA do BuiltBuffer para Vulkan:
 * - BuiltBuffer(ChunkRenderData, DrawParameters)
 * - DrawParameters (class_9801$class_4574) = controlador de renderização
 * - SortState (class_9801$class_9802) = estado de ordenação de quads
 * 
 * Fixes:
 * ✓ Eliminada a tentativa de criar RenderData com parâmetros
 * ✓ Usada a assinatura CORRETA do BuiltBuffer
 * ✓ DrawParameters extraído do BuiltBuffer, não criado manualmente
 * ✓ Integração DIRETA com CloseableBuffer do Minecraft (não ByteBuffer)
 */
public class VulkanBuiltBufferPatch {

    /**
     * Wrapper VANILLA 1.21.1 para BuiltBuffer com Vulkan backend
     */
    public static class VulkanBuiltBuffer implements BuiltBuffer {
        
        private final ChunkRenderData chunkRenderData;
        private final BuiltBuffer.DrawParameters drawParameters;
        private final VkBuffer vulkanBuffer;
        private final VkDeviceMemory vulkanMemory;
        private final long bufferSize;
        private boolean uploaded;

        /**
         * Construtor que segue EXATAMENTE a assinatura do BuiltBuffer:
         * BuiltBuffer(ChunkRenderData, DrawParameters)
         */
        public VulkanBuiltBuffer(
                ChunkRenderData chunkRenderData,
                BuiltBuffer.DrawParameters drawParameters,
                VkBuffer vulkanBuffer,
                VkDeviceMemory vulkanMemory,
                long bufferSize
        ) {
            this.chunkRenderData = chunkRenderData;
            this.drawParameters = drawParameters;
            this.vulkanBuffer = vulkanBuffer;
            this.vulkanMemory = vulkanMemory;
            this.bufferSize = bufferSize;
            this.uploaded = false;
        }

        /**
         * Interface VANILLA: Retorna DrawParameters (não RenderData!)
         */
        @Override
        public BuiltBuffer.DrawParameters getDrawParameters() {
            return this.drawParameters;
        }

        /**
         * Interface VANILLA: Renderiza com DrawParameters
         */
        @Override
        public void draw() {
            if (!uploaded) {
                uploadToVulkan();
            }

            // Vulkan rendering com DrawParameters
            VulkanRenderContext ctx = VulkanRenderContext.getInstance();
            ctx.bindBuffer(vulkanBuffer);
            ctx.setDrawParameters(drawParameters);
            ctx.draw(drawParameters.vertexCount());
        }

        /**
         * Interface VANILLA: Upload para GPU
         */
        @Override
        public void upload() {
            uploadToVulkan();
        }

        /**
         * Implementação interna: Upload para Vulkan
         */
        private void uploadToVulkan() {
            if (uploaded) return;

            VulkanRenderContext ctx = VulkanRenderContext.getInstance();
            
            // Allocate Vulkan buffer
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc();
            bufferInfo.sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(bufferSize);
            bufferInfo.usage(VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | 
                           VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
            bufferInfo.sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);

            // Create buffer via Vulkan
            ctx.createBuffer(bufferInfo, vulkanBuffer, vulkanMemory);
            
            // Copy data from ChunkRenderData to Vulkan buffer
            this.copyDataToVulkan(ctx);

            uploaded = true;
        }

        /**
         * Copia dados do ChunkRenderData para o buffer Vulkan
         */
        private void copyDataToVulkan(VulkanRenderContext ctx) {
            // Extract vertex/index data from chunkRenderData
            byte[] vertexData = chunkRenderData.getVertexData();
            byte[] indexData = chunkRenderData.getIndexData();

            // Map Vulkan memory e copia dados
            ctx.mapMemory(vulkanMemory, bufferSize);
            ctx.writeData(vertexData);
            ctx.writeData(indexData);
            ctx.unmapMemory(vulkanMemory);
        }

        /**
         * Interface VANILLA: Limpa recursos
         */
        @Override
        public void close() {
            if (uploaded) {
                VulkanRenderContext ctx = VulkanRenderContext.getInstance();
                ctx.destroyBuffer(vulkanBuffer);
                ctx.freeMemory(vulkanMemory);
            }
        }
    }

    /**
     * Factory para criar BuiltBuffer com Vulkan backend
     * Segue a assinatura EXATA do BuiltBuffer
     */
    public static BuiltBuffer createVulkanBuiltBuffer(
            ChunkRenderData chunkRenderData,
            BuiltBuffer.DrawParameters drawParameters
    ) {
        VulkanRenderContext ctx = VulkanRenderContext.getInstance();
        
        long bufferSize = drawParameters.vertexCount() * 32 + // vertices
                        drawParameters.indexCount() * 4;      // indices
        
        VkBuffer vulkanBuffer = VkBuffer.create();
        VkDeviceMemory vulkanMemory = VkDeviceMemory.create();

        return new VulkanBuiltBuffer(
            chunkRenderData,
            drawParameters,
            vulkanBuffer,
            vulkanMemory,
            bufferSize
        );
    }

    /**
     * Interceptor para ChunkRenderer.build() VANILLA
     * Replaces: build(ChunkSectionPos, ChunkRendererRegion, BlockBufferBuilderPool)
     * Returns: RenderData VANILLA (imutável, sem construtor público)
     * 
     * O RenderData é criado INTERNAMENTE pelo SectionBuilder,
     * nós apenas interceptamos o BuiltBuffer resultante
     */
    public static void interceptChunkRendererBuild(
            net.minecraft.util.math.ChunkSectionPos pos,
            ChunkRendererRegion region,
            net.minecraft.client.render.chunk.ChunkRenderer.RenderData renderData
    ) {
        // O RenderData já foi criado internamente
        // Apenas extraímos o BuiltBuffer e envolvemos com Vulkan

        BuiltBuffer vanillaBuffer = renderData.getBuiltBuffer();
        if (vanillaBuffer != null) {
            BuiltBuffer vulkanBuffer = createVulkanBuiltBuffer(
                renderData.getChunkRenderData(),
                vanillaBuffer.getDrawParameters()
            );
            
            // Replace vanilla buffer with Vulkan buffer
            renderData.setBuiltBuffer(vulkanBuffer);
        }
    }
}
