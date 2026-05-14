#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <atomic>

#define LOG_TAG "NEFU"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static constexpr int RENDERER_LTW         = 0;
static constexpr int RENDERER_MOBILEGLUES = 1;
static constexpr int RENDERER_KRYPTON     = 2;
static constexpr int RENDERER_ZINK        = 3;
static constexpr int RENDERER_PASSTHROUGH = 4;

#ifdef NEFU_HAS_LTW
  extern "C" void ltw_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif
#ifdef NEFU_HAS_MOBILEGLUES
  extern "C" void mobileglues_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif
#ifdef NEFU_HAS_KRYPTON
  extern "C" void krypton_glDrawArrays(GLenum mode, GLint first, GLsizei count);
#endif

typedef void (*DrawArraysFn)(GLenum, GLint, GLsizei);
static void draw_passthrough(GLenum m, GLint f, GLsizei c) { glDrawArrays(m, f, c); }

static DrawArraysFn draw_table[5] = {
    draw_passthrough, draw_passthrough, draw_passthrough,
    draw_passthrough, draw_passthrough,
};
static std::atomic<int>  current_renderer{RENDERER_PASSTHROUGH};
static std::atomic<bool> nefu_active{false};

static void populate_draw_table() {
#ifdef NEFU_HAS_LTW
    draw_table[RENDERER_LTW] = ltw_glDrawArrays;
    LOGI("LTW translator: active");
#endif
#ifdef NEFU_HAS_MOBILEGLUES
    draw_table[RENDERER_MOBILEGLUES] = mobileglues_glDrawArrays;
    LOGI("MobileGlues translator: active");
#endif
#ifdef NEFU_HAS_KRYPTON
    draw_table[RENDERER_KRYPTON] = krypton_glDrawArrays;
    LOGI("Krypton translator: active");
#endif
}

static inline void do_batched_draw(int renderer, GLenum mode,
                                   const jint* firsts, const jint* counts, jsize len) {
    DrawArraysFn fn = draw_table[renderer];
    for (jsize i = 0; i < len; ++i)
        fn(mode, static_cast<GLint>(firsts[i]), static_cast<GLsizei>(counts[i]));
}

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeInit(JNIEnv*, jclass) {
    populate_draw_table();
    nefu_active.store(true);
    LOGI("NEFU v1.0 -- native core ready.");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeSelectRenderer(JNIEnv*, jclass, jint rendererId) {
    if (rendererId < 0 || rendererId > RENDERER_PASSTHROUGH) {
        LOGW("nativeSelectRenderer: invalid id %d -- using PASSTHROUGH.", rendererId);
        rendererId = RENDERER_PASSTHROUGH;
    }
    current_renderer.store(rendererId);
    LOGI("Active renderer: %d", rendererId);
}

JNIEXPORT void JNICALL
Java_com_nexus_nefu_NefuCoreEngine_nativeDrawArraysBatched(
    JNIEnv* env, jclass,
    jint mode, jintArray jFirsts, jintArray jCounts, jint renderer)
{
    if (!nefu_active.load()) return;
    jsize len = env->GetArrayLength(jFirsts);
    if (len <= 0) return;
    jint* firsts = env->GetIntArrayElements(jFirsts, nullptr);
    jint* counts = env->GetIntArrayElements(jCounts, nullptr);
    if (!firsts || !counts) {
        LOGE("nativeDrawArraysBatched: failed to pin JNI arrays.");
        if (firsts) env->ReleaseIntArrayElements(jFirsts, firsts, JNI_ABORT);
        if (counts) env->ReleaseIntArrayElements(jCounts, counts, JNI_ABORT);
        return;
    }
    do_batched_draw(renderer, static_cast<GLenum>(mode), firsts, counts, len);
    env->ReleaseIntArrayElements(jFirsts, firsts, JNI_ABORT);
    env->ReleaseIntArrayElements(jCounts, counts, JNI_ABORT);
}

} // extern "C"
