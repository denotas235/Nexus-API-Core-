#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <atomic>
#include <cstring>

#define LOG_TAG "NEFU"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Renderer IDs (must match NefuCoreEngine.java)
static constexpr int RENDERER_LTW         = 0;
static constexpr int RENDERER_MOBILEGLUES = 1;
static constexpr int RENDERER_KRYPTON     = 2;
static constexpr int RENDERER_ZINK        = 3;
static constexpr int RENDERER_PASSTHROUGH = 4;

// ── Translator entry points ───────────────────────────────────────────────────
#ifdef NEFU_HAS_LTW
  extern "C" void ltw_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif
#ifdef NEFU_HAS_MOBILEGLUES
  extern "C" void mobileglues_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif
#ifdef NEFU_HAS_KRYPTON
  extern "C" void krypton_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif

// ── Draw-function table ───────────────────────────────────────────────────────
typedef void (*DrawFn)(GLenum, GLint, GLsizei);
static void draw_passthrough(GLenum m, GLint f, GLsizei c) { glDrawArrays(m, f, c); }

static DrawFn draw_table[5] = {
    draw_passthrough,   // LTW         — filled by populate_draw_table if available
    draw_passthrough,   // MOBILEGLUES — filled by populate_draw_table if available
    draw_passthrough,   // KRYPTON     — filled by populate_draw_table if available
    draw_passthrough,   // ZINK        — future Vulkan bridge
    draw_passthrough,   // PASSTHROUGH — always glDrawArrays
};

// ── TBDR Mali extension flags ─────────────────────────────────────────────────
static bool has_framebuffer_fetch = false;
static bool has_buffer_storage    = false;

// ── State ─────────────────────────────────────────────────────────────────────
static std::atomic<int>  current_renderer{RENDERER_PASSTHROUGH};
static std::atomic<bool> nefu_active{false};

// ── Helpers ───────────────────────────────────────────────────────────────────
static bool check_ext(const char* list, const char* ext) {
    return (list && ext) ? (strstr(list, ext) != nullptr) : false;
}

static void populate_draw_table() {
#ifdef NEFU_HAS_LTW
    draw_table[RENDERER_LTW]         = ltw_glDrawArrays;
    LOGI("Translator active: LTW");
#endif
#ifdef NEFU_HAS_MOBILEGLUES
    draw_table[RENDERER_MOBILEGLUES] = mobileglues_glDrawArrays;
    LOGI("Translator active: MobileGlues");
#endif
#ifdef NEFU_HAS_KRYPTON
    draw_table[RENDERER_KRYPTON]     = krypton_glDrawArrays;
    LOGI("Translator active: Krypton");
#endif
}

static void detect_tbdr_extensions() {
    const char* ext = (const char*)glGetString(GL_EXTENSIONS);
    has_framebuffer_fetch = check_ext(ext, "GL_ARM_shader_framebuffer_fetch");
    has_buffer_storage    = check_ext(ext, "GL_EXT_buffer_storage");
    LOGI("TBDR GL_ARM_shader_framebuffer_fetch: %d", (int)has_framebuffer_fetch);
    LOGI("TBDR GL_EXT_buffer_storage          : %d", (int)has_buffer_storage);
}

// TBDR-aware batched draw: all calls share the same primitive mode so the Mali
// tile-scheduler can merge them within a single tile pass.
static inline void do_batch(int renderer, GLenum mode,
                             const jint* firsts, const jint* counts, jsize len) {
    DrawFn fn = draw_table[renderer];
    for (jsize i = 0; i < len; ++i) {
        fn(mode, static_cast<GLint>(firsts[i]), static_cast<GLsizei>(counts[i]));
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// JNI exports
// ═════════════════════════════════════════════════════════════════════════════
extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeInit(JNIEnv*, jclass) {
    populate_draw_table();
    detect_tbdr_extensions();
    nefu_active.store(true);
    LOGI("NEFU v1.0 -- native core ready.");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeSelectRenderer(JNIEnv*, jclass, jint id) {
    if (id < 0 || id > RENDERER_PASSTHROUGH) {
        LOGW("nativeSelectRenderer: invalid id %d -- using PASSTHROUGH.", id);
        id = RENDERER_PASSTHROUGH;
    }
    current_renderer.store(id);
    LOGI("Active renderer set to: %d", id);
}

JNIEXPORT void JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeBatchedDraw(
    JNIEnv* env, jclass,
    jint mode, jintArray jFirsts, jintArray jCounts, jint renderer)
{
    if (!nefu_active.load()) return;

    jsize len = env->GetArrayLength(jFirsts);
    if (len <= 0) return;

    jint* firsts = env->GetIntArrayElements(jFirsts, nullptr);
    jint* counts = env->GetIntArrayElements(jCounts,  nullptr);

    if (!firsts || !counts) {
        LOGE("nativeBatchedDraw: failed to pin JNI arrays -- skipping batch.");
        if (firsts) env->ReleaseIntArrayElements(jFirsts, firsts, JNI_ABORT);
        if (counts) env->ReleaseIntArrayElements(jCounts,  counts, JNI_ABORT);
        return;
    }

    do_batch(renderer, static_cast<GLenum>(mode), firsts, counts, len);

    env->ReleaseIntArrayElements(jFirsts, firsts, JNI_ABORT);
    env->ReleaseIntArrayElements(jCounts,  counts, JNI_ABORT);
}

JNIEXPORT jboolean JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeHasFramebufferFetch(JNIEnv*, jclass) {
    return has_framebuffer_fetch ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeHasBufferStorage(JNIEnv*, jclass) {
    return has_buffer_storage ? JNI_TRUE : JNI_FALSE;
}

} // extern "C"
