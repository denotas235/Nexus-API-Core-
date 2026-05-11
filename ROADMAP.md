# Roadmap — Nexus Streaming

## Estado Atual (v1.0.0)

- `StreamingPipeline` deteta `GL_ARB_buffer_storage` / `OpenGL44` e `GL_ARB_map_buffer_range` / `OpenGL30`
- `ChunkBufferManager`: aloca buffers persistentes por chunk via `GL44.glBufferStorage()` com flags `MAP_WRITE | MAP_PERSISTENT | MAP_COHERENT`; fallback automático para `glBufferData` (DYNAMIC_DRAW) se a extensão não existir
- `IncrementalUploader`: escreve diretamente no `ByteBuffer` mapeado (zero cópias) ou via `glBufferSubData` (fallback)
- `UploadQueue`: fila thread-safe (`ConcurrentLinkedQueue`) processada a 4 uploads/tick sem bloquear a render thread
- `ChunkBuilderMixin`: pré-aloca o buffer persistente assim que o chunk é agendado para rebuild (`require=0`)
- `DebugHudMixin`: status em tempo real no ecrã F3 (extensões, uploads pendentes/totais)
- CI GitHub Actions: build automático em todos os pushes para `perf/streaming`

### O que ainda não está feito em v1.0.0

- **Dados reais de geometria**: o buffer é pré-alocado mas a cena ainda não escreve vertices nele. Isso requer intercetar o chunk mesh builder, que varia entre versões. Chega na v1.1.0.
- **Atlas dinâmico**: a sub-regiao de textura (`glTexSubImage2D`) está implementada mas não integrada com o atlas de texturas do Minecraft.

---

## Planos Futuros

### v1.1.0 — Geometria Real nos Buffers

**Motivação:** Os buffers estão alocados mas vazios — o streaming incremental ainda não é aplicado.

- Intercetar `BuiltChunkStorage.upload()` ou equivalente Yarn 1.21.1
- Comparar a nova geometria com a versão anterior (differential update)
- Enviar apenas os vértices alterados via `IncrementalUploader.uploadPartial()`
- Redução de 40-80% no tráfego GPU por frame

---

### v1.2.0 — Atlas de Texturas Incremental

**Motivação:** O Minecraft reenvia o atlas completo a cada reload de recursos.

- Intercetar `SpriteAtlasTexture.upload()` para detetar sprites alterados
- Usar `IncrementalUploader.uploadTextureRegion()` para enviar apenas as regiões modificadas
- Integração com `nexus-textures`: atlas ASTC enviado incrementalmente
- Redução de 70-90% no tráfego de texturas a cada reload

---

### v1.3.0 — Gestão de Memória por Render Distance

**Motivação:** Chunks que saem do render distance devem libertar os seus buffers imediatamente.

- Listener `ChunkUpdateEvent` ou equivalente para detetar chunks descarregados
- Chamar `ChunkBufferManager.free(chunkKey)` automaticamente
- Reutilização de buffers libertados (pool) para novos chunks
- VRAM estável mesmo em exploração contínua

---

### v1.4.0 — Configuração In-Game

**Motivação:** O comportamento ideal varia com o dispositivo.

- Uploads por tick: 1 / 4 / 8 (consoante o FPS alvo)
- Tamanho inicial do buffer: 32 KB / 64 KB / 128 KB por chunk
- Toggle buffer storage (forçar fallback para testes)
- Persistência em `config/nexus-streaming.json`

---

### v1.5.0 — Integração com nexus-shadows

**Motivação:** O shadow map usa a mesma geometria dos chunks — duplicar buffers é desperdício.

- Reutilizar os buffers persistentes do streaming para o shadow pass
- Shadow map alimentado diretamente pelos buffers incrementais
- Eliminar a cópia CPU→GPU do shadow pass

---

### v2.0.0 — Streaming Assíncrono com Fence Sync

**Motivação:** Uploads no tick bloqueiam a render thread se o GPU estiver ocupado.

- `glFenceSync` após cada upload para detetar quando o GPU terminou
- Fila prioritária: chunks à frente do jogador têm prioridade máxima
- Métricas de latência exportadas para o F3 (ms por upload, chunks em fila)
- Redução de latência de chunk de 50-200ms para <1ms com buffers persistentes