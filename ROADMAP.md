# Roadmap — Nexus Shadows

## Estado Atual (v1.0.0)

- `GameRendererMixin` garante init GL segura no primeiro frame (sem crash)
- FBO de profundidade 1024×1024 criado e verificado (`glCheckFramebufferStatus`)
- `ShadowMapShader` com duplo fallback GLSL: `#version 330 core` → `#version 310 es`
- `ShadowPCFShader` com duplo fallback GLSL + verificação individual de cada shader
- `WorldRendererMixin` registado em `mixins.json` com assinatura correta MC 1.21.1 Yarn
- `DebugHudMixin` injeta status real no ecrã F3 (shadow map, PCF, shader, ângulo do sol)
- SLF4J Logger em todas as classes (sem `System.out.println`)
- Pipeline fallsafe: qualquer falha GL é logada sem crash

### O que ainda não está feito em v1.0.0

- **Geometria no shadow pass**: o FBO é criado e o shader compila, mas a cena não é renderizada do ponto de vista da luz. O draw call real chega na v1.1.0.
- **Sombras de entidades**: requer intercetar o render de entidades separadamente.

---

## Planos Futuros

### v1.1.0 — Geometria Real no Shadow Pass

**Motivação:** O FBO está pronto mas vazio — as sombras ainda não aparecem no jogo.

- `ChunkShadowRenderer`: renderiza os chunks do ponto de vista da luz usando `lightSpaceMatrix`
- Intercetar `WorldRenderer.renderChunks()` para redirecionar para o shadow FBO
- Passar `uLightSpaceMatrix` ao shader de blocos durante o shadow pass
- Sombras de blocos visíveis no chão (árvores, edifícios, montanhas)

---

### v1.2.0 — Sombras de Entidades

**Motivação:** Mobs e jogadores não projetam sombra na v1.1.0.

- Intercetar `EntityRenderDispatcher.render()` durante o shadow pass
- Renderizar entidades com o `ShadowMapShader` (apenas depth)
- Sombras de mobs, jogadores e itens no chão

---

### v1.3.0 — Intensidade Variável com o Sol

**Motivação:** As sombras devem ser mais suaves ao nascer/pôr do sol, mais fortes ao meio-dia.

- Calcular intensidade com base no ângulo do sol (`getSkyAngle`)
- Uniform `uShadowIntensity` no PCF shader (0.2 ao amanhecer → 0.6 ao meio-dia)
- Sombras desaparecem gradualmente à noite

---

### v1.4.0 — Configuração In-Game

**Motivação:** Permitir ao jogador ajustar as sombras sem editar ficheiros.

- Resolução do shadow map: 256 / 512 / 1024 / 2048
- Raio da luz ortográfica: 20 / 40 / 80 blocos
- Toggle PCF: ON / OFF
- Persistência em `config/nexus-shadows.json`

---

### v1.5.0 — Integração com nexus-render-hdr

**Motivação:** Sombras aplicadas após tonemapping perdem fidelidade de cor.

- Pipeline unificado: shadow pass → tonemapping ACES → PCF composite
- Sombras com gradação de cor correta (sem achatamento pelo gamma)
- API pública para outros mods injetarem passes de sombra

---

### v2.0.0 — PCSS (Percentage Closer Soft Shadows)

**Motivação:** PCF 3×3 produz bordas de sombra de tamanho fixo.

- PCSS: bordas mais largas para objetos distantes da fonte de luz
- Penumbra realista com raio proporcional à distância
- Compatível com `nexus-render-hdr` e `nexus-streaming`