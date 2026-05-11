# Roadmap — Nexus Render HDR

## Estado Atual (v1.0.0)

- Inicialização GL segura via `GameRendererMixin` (primeiro frame, não em `onInitializeClient`)
- Correção de espaço de cor: `GL_FRAMEBUFFER_SRGB` ativado se suportado pelo driver
- Filtragem anisotrópica automática (16x) via `AbstractTexture.bindTexture()`
- Shader ACES compilado com duplo fallback GLSL: `#version 330 core` → `#version 310 es`
- Mini HUD no ecrã F3 com status real de cada funcionalidade
- Deteção de mods complementares: nexus-textures, nexus-shadows, nexus-api-core
- Fallsafe em todos os passos: qualquer falha GL é logada sem crash

---

## Planos Futuros

### v1.1.0 — Shader ACES Aplicado ao Framebuffer

**Motivação:** O shader ACES está compilado mas ainda não é aplicado ao render final.

- Intercetar o framebuffer final via `GameRendererMixin` ou Fabric `WorldRenderEvents`
- Renderizar o quad ACES sobre o framebuffer de forma não-destrutiva
- Opção de intensidade configurável (0.0 = desligado, 1.0 = full ACES)
- Preview em tempo real com tecla de atalho (ex: `F8`)

---

### v1.2.0 — Configuração In-Game

**Motivação:** Permitir ao jogador ajustar os efeitos sem editar ficheiros.

- Ecrã de configuração acessível via mods de opções (ModMenu)
- Slider de intensidade ACES
- Toggle sRGB por situação (overworld, nether, end)
- Configuração de anisotropic: 1x / 2x / 4x / 8x / 16x
- Persistência em `config/nexus-render-hdr.json`

---

### v1.3.0 — Bloom e Emissivos

**Motivação:** Blocos emissivos (lava, tochas, cogumelos brilhantes) sem intensidade controlada.

- Detetar texturas emissivas via ASTC HDR (integração com nexus-textures)
- Bloom físico: extração de highlights + blur gaussiano multi-pass
- Limiar de emissão configurável
- Compatível com resource packs que usam emissive maps

---

### v1.4.0 — Ambient Occlusion melhorado

**Motivação:** O AO vanilla é muito grosseiro e sem gradação.

- SSAO (Screen-Space Ambient Occlusion) via shader de pós-processamento
- Raio e intensidade configuráveis
- Integração com a profundidade do G-buffer de Minecraft

---

### v1.5.0 — Integração com nexus-shadows

**Motivação:** Sombras dinâmicas do nexus-shadows sem correção de cor ficam com artefactos.

- Pipeline unificado: sombras passam pelo tonemapping ACES
- Suporte a soft shadows com penumbra realista
- Correção de cor aplicada à textura de sombra antes de composite

---

### v2.0.0 — Pipeline HDR Completo

**Motivação:** Visão final do ecossistema Nexus.

- G-buffer com depth, normals, albedo e emissão em separado
- Tonemapping ACES + bloom + SSAO + sombras num único pass de pós-processamento
- API pública para outros mods injetarem passes de render
- Suporte a HDR display (se o dispositivo suportar)