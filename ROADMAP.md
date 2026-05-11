# Roadmap — Nexus Textures

## Estado Atual (v1.0.0)

- Compressão ASTC offline de todas as texturas vanilla (2964 texturas pré-compiladas no JAR)
- Compressão ASTC em runtime via `astcenc-neon` (ARM64) para resource packs externos
- Cache de resultados runtime com recompressão de alta qualidade em background
- Ecrã de carregamento HUD (barra de progresso, 6 segundos após inicialização)
- Debug info no ecrã F3 (status, contagem, encoder, tempo de carregamento)
- Deteção automática e limpeza de cache ao recarregar resource packs

---

## Planos Futuros

### v1.1.0 — Suporte Multi-Arquitetura

**Motivação:** Permitir usar o mod em emuladores x86_64 e em futuros dispositivos ARM com SVE.

- Deteção automática de arquitetura em runtime (`os.arch`, `/proc/cpuinfo`)
- Embalar múltiplos binários no JAR:
  - `natives/arm64-v8a/libastcenc.so` — NEON (atual, Mali-G52 MC2)
  - `natives/x86_64/libastcenc.so` — AVX2 (emuladores)
  - `natives/arm64-v8a/libastcenc-sve.so` — SVE 128-bit (opcional, futuro)
- Seleção automática do binário correto na inicialização
- Fallback gracioso se nenhum binário for compatível

---

### v1.2.0 — Compressão Paralela e Progressiva

**Motivação:** Reduzir o tempo de compressão em runtime de resource packs grandes.

- Pool de threads configurável para compressão paralela (atualmente single-threaded via `BackgroundRecompressor`)
- Priorização de texturas visíveis no viewport
- Modo "fast first, quality later": mostrar textura em qualidade rápida imediatamente, substituir em background com qualidade thorough
- Barra de progresso granular por categoria (blocos, entidades, GUI, partículas)

---

### v1.3.0 — Cache Persistente em Disco

**Motivação:** Evitar recomprimir texturas de resource packs a cada sessão.

- Cache persistente em `~/.minecraft/nexus-astc-cache/`
- Chave de cache baseada em hash do PNG original (SHA-256)
- Invalidação automática quando o resource pack é alterado
- Limite de tamanho configurável com LRU eviction
- Estatísticas de hit/miss no ecrã F3

---

### v1.4.0 — Suporte a Atlases e Mipmaps

**Motivação:** Cobrir texturas compostas e níveis de detalhe para ganhos completos de VRAM.

- Intercetar geração de atlas de texturas (`TextureAtlas`) e comprimir o atlas inteiro como ASTC
- Gerar mipmaps ASTC para texturas de distância (blocos, terreno)
- Suporte a formatos ASTC HDR para texturas emissivas e efeitos

---

### v2.0.0 — API Pública para Outros Mods

**Motivação:** Permitir que mods do ecossistema Nexus (shadows, HDR, etc.) usem o pipeline ASTC.

- API pública estável: `NexusTextureAPI.compress()`, `NexusTextureAPI.getRegistry()`
- Eventos Fabric para notificar outros mods quando texturas são comprimidas
- Integração nativa com `mod-nexus-render-hdr` e `mod-nexus-shadows`
- Documentação de integração para modders terceiros