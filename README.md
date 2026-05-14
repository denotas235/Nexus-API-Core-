# NexusFusion Render — NEFU

> Renderizador híbrido para Minecraft: Java Edition (Fabric),
> optimizado para GPUs Mali com arquitectura TBDR.

[![Build](https://github.com/denotas235/Nexus-API-Core-/actions/workflows/build-nefu.yml/badge.svg?branch=render%2Fnefu-core)](https://github.com/denotas235/Nexus-API-Core-/actions/workflows/build-nefu.yml)
![License](https://img.shields.io/badge/license-MIT-green)
![MC](https://img.shields.io/badge/minecraft-1.21.1-brightgreen)
![Loader](https://img.shields.io/badge/loader-Fabric%200.16%2B-blueviolet)

---

## O que é o NEFU?

O NEFU é uma camada de **tradução inteligente** que intercepta os comandos
OpenGL emitidos pelo Minecraft e os encaminha para o tradutor mais eficiente
para o hardware detectado automaticamente.

```
Minecraft (OpenGL Desktop)
         │
         ▼
  RenderSystemMixin ── intercepta draw calls
         │
         ▼
   NefuCoreEngine ── TierManager ──► GPU Tier (T0–T5)
         │
         ├─ T0/T1 ─► LTW         (wrapper GL→GLES rápido)
         ├─ T2    ─► MobileGlues  (tradução de shaders + TBDR)
         ├─ T3    ─► Krypton      (NG-GL4ES, fallback robusto)
         ├─ T4    ─► Zink         (ponte Vulkan via SPIR-V)
         └─ T5+   ─► Passthrough  (PC, sem tradução)
                │
                ▼  se falhar → FallbackHandler tenta o próximo
```

---

## Funcionalidades

| Módulo | Responsabilidade |
|--------|-----------------|
| **BatchManager** | Agrupa draw calls consecutivos do mesmo modo GL, reduzindo até 70% das chamadas ao driver |
| **TierManager** | Detecta automaticamente o Tier da GPU (T0–T5) pelo GL renderer string |
| **ShaderOrchestrator** | Analisa o GLSL e escolhe o tradutor ideal por shader |
| **FallbackHandler** | Cadeia de recuperação: se um tradutor falhar, tenta o próximo |
| **HardwareInfo** | Escreve `nefu_device_profile.json` com toda a informação da GPU |
| **ModCompatibility** | Detecta Sodium/Iris (compatíveis) e OptiFabric/Canvas (incompatíveis) |
| **TBDR Extensions** | Detecta `GL_ARM_shader_framebuffer_fetch` e `GL_EXT_buffer_storage` |

---

## Tiers de Hardware

| Tier | Exemplos de GPU | Tradutor Activo |
|------|----------------|-----------------|
| T0 | Mali-400, Adreno 200 | LTW |
| T1 | Mali-T720, Adreno 506 | LTW + batching |
| **T2** | **Mali-G52 MC2 ← alvo principal** | **MobileGlues + TBDR** |
| T3 | Mali-G76, Adreno 650 | Krypton |
| T4 | Adreno 7xx, SD 8 Gen | Zink (Vulkan) |
| T5 | PC (NVIDIA/AMD/Intel) | Passthrough |

---

## Optimizações TBDR (Mali)

A arquitectura TBDR da Mali armazena cor e profundidade em memória
*on-chip* ultra-rápida (Tile Memory). O NEFU explora duas extensões:

| Extensão | Benefício |
|----------|-----------|
| `GL_ARM_shader_framebuffer_fetch` | Shaders lêem o framebuffer directamente da Tile Memory sem ir à VRAM — ideal para blending, partículas, iluminação deferred |
| `GL_EXT_buffer_storage` | VBOs estáticos (chunks) ficam num cache agressivo da GPU — reduz latência de geometria |

O ficheiro `nefu_device_profile.json` informa quais estão disponíveis no
teu dispositivo.

---

## Compatibilidade com outros mods

| Mod | Estado | Notas |
|-----|--------|-------|
| **Sodium** | ✅ Compatível | NEFU desactiva o seu batching e delega ao Sodium |
| **Iris** | ✅ Compatível | Gestão de shaders transferida para o Iris |
| **Indium** | ✅ Compatível | Addon do Sodium, sem conflito |
| **OptiFabric** | ❌ Incompatível | Conflito na pipeline GL |
| **OptiFine** | ❌ Incompatível | Incompatível com Fabric |
| **Canvas** | ❌ Incompatível | Substitui a pipeline inteira |

---

## Sistema de Segurança (Crash-Safe)

O NEFU foi desenhado para **nunca quebrar o jogo**:

1. `System.loadLibrary("nefu")` falha → `active = false` → todos os mixins são no-ops.
2. Tradutor falha → `FallbackHandler` tenta o próximo na hierarquia.
3. Tudo falha → `PASSTHROUGH` → a chamada GL original chega ao driver intacta.
4. Mixins com `require = 0` + `defaultRequire: 0` → não crasham se o método alvo mudar entre versões MC.
5. Toda a init GL (HardwareInfo, NefuCoreEngine) é diferida para `ClientLifecycleEvents.CLIENT_STARTED` — nunca corre antes do contexto GL estar pronto.
6. Todos os acessos a strings GL são null-safe (tanto em Java como em C++).

---

## Requisitos

| Componente | Versão |
|------------|--------|
| Minecraft | 1.21.1 |
| Fabric Loader | ≥ 0.16.0 |
| Fabric API | 0.116.11+1.21.1 |
| Java | ≥ 21 |

---

## Instalação

1. Faz download do JAR na página de [Releases](../../releases).
2. Copia o JAR para a pasta `mods/` do Minecraft.
3. Inicia o jogo — o NEFU detecta automaticamente o hardware.
4. Verifica `nefu_device_profile.json` na pasta do jogo para confirmar.

---

## Compilar a partir do código

```bash
git clone -b render/nefu-core https://github.com/denotas235/Nexus-API-Core-.git
cd Nexus-API-Core-
./gradlew :nexus-nefu:build --no-daemon
# JAR em: nexus-nefu/build/libs/nexus-nefu-*.jar
```

### Compilar o código nativo (Android NDK)

```bash
# Clonar os tradutores
git clone https://github.com/MojoLauncher/LTW         nexus-nefu/src/main/cpp/ltw
git clone https://github.com/MobileGL-Dev/MobileGlues nexus-nefu/src/main/cpp/mobileglues
git clone https://github.com/BZLZHH/NG-GL4ES          nexus-nefu/src/main/cpp/krypton

# Compilar com NDK (arm64-v8a, API 26+)
cmake -DCMAKE_TOOLCHAIN_FILE=NDK/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-26 \
      -B nexus-nefu/src/main/cpp/build \
      nexus-nefu/src/main/cpp
cmake --build nexus-nefu/src/main/cpp/build
```

---

## Estrutura do Projecto

```
nexus-nefu/src/main/
├── java/com/nexus/nefu/
│   ├── NexusNefuClient.java       Entry-point Fabric (2 fases de init)
│   ├── NefuCoreEngine.java        Dispatcher JNI + gestão de renderers
│   ├── BatchManager.java          Agrupamento de draw calls
│   ├── TierManager.java           Detecção de Tier da GPU
│   ├── HardwareInfo.java          Perfil de hardware (JSON)
│   ├── ModCompatibility.java      Verificação de conflitos
│   ├── NefuConfig.java            Configuração unificada
│   ├── ShaderOrchestrator.java    Selecção de tradutor por shader GLSL
│   ├── FallbackHandler.java       Cadeia de recuperação de erros
│   └── mixin/
│       ├── RenderSystemMixin.java  Intercepta draw calls (require=0)
│       └── WorldRendererMixin.java Flush de batch no fim do frame
├── resources/
│   ├── fabric.mod.json            MC 1.21.1, Fabric ≥ 0.16.0
│   └── nexus-nefu.mixins.json     JAVA_21, defaultRequire=0
└── cpp/
    ├── CMakeLists.txt             Flags ARM64, auto-detect tradutores
    ├── nefu_core.cpp              JNI + TBDR extension detection
    ├── ltw/                       Clone do LTW
    ├── mobileglues/               Clone do MobileGlues
    ├── krypton/                   Clone do Krypton (NG-GL4ES)
    ├── zink/                      Clone do Mesa Zink
    └── virgl/                     Clone do VirGL
```

---

## Criar um Release

```bash
git tag v1.0.0
git push origin v1.0.0
# O GitHub Actions compila e publica automaticamente o Release com o JAR.
```

---

## Configuração

| Campo | Padrão | Descrição |
|-------|--------|-----------|
| `batchingEnabled` | `true` | Agrupar draw calls (auto-`false` com Sodium) |
| `tierOverride` | `-1` | Forçar Tier (-1 = auto-detect) |
| `hdr` | `false` | Pipeline HDR (GL_RGBA16F) |
| `shadowQuality` | `1` | Sombras: 0=off 1=low 2=med 3=high |
| `useZink` | `false` | Usar Zink (Vulkan) em T4+ |
| `mobilegluesEnableAngle` | `false` | ANGLE back-end no MobileGlues |
| `mobilegluesMaxGlslCacheMb` | `32` | Cache de shaders MobileGlues (MB) |
| `mobilegluesCustomGLVersion` | `"4.0.0"` | Versão GL reportada ao shader |
| `mobilegluesShaderDebug` | `false` | Logs detalhados de compilação |
| `tbdrFramebufferFetch` | `true` | GL_ARM_shader_framebuffer_fetch |
| `tbdrBufferStorage` | `true` | GL_EXT_buffer_storage |

---

## Autor

**Antonio (SerpentSpirale / CADIndie)**
GPU alvo: Mali-G52 MC2 (Helio G85) — OpenGL ES 3.2, Vulkan 1.1

---

## Licença

MIT © Antonio (SerpentSpirale)
