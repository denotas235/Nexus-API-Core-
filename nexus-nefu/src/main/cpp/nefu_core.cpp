#include <jni.h>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <android/log.h>
#include <vector>

#define LOG_TAG "NEFU"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

typedef void (*draw_arrays_t)(GLenum mode, GLint first, GLsizei count);

void ltw_drawArrays(GLenum mode, GLint first, GLsizei count) {
    glDrawArrays(mode, first, count);
}
void mobileglues_drawArrays(GLenum mode, GLint first, GLsizei count) {
    glDrawArrays(mode, first, count);
}
void krypton_drawArrays(GLenum mode, GLint first, GLsizei count) {
    glDrawArrays(mode, first, count);
}
void zink_drawArrays(GLenum mode, GLint first, GLsizei count) {
    glDrawArrays(mode, first, count);
}

static draw_arrays_t current_draw = glDrawArrays;
static int current_renderer = 4;

extern "C" {

JNIEXPORT jboolean JNICALL Java_com_nexus_nefu_NefuCoreEngine_nativeInit(JNIEnv* env, jobject thiz) {
    LOGI("NEFU native core initialized.");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_com_nexus_nefu_NefuCoreEngine_nativeSelectRenderer(JNIEnv* env, jobject thiz, jint rendererId) {
    current_renderer = rendererId;
    switch (rendererId) {
        case 0: current_draw = ltw_drawArrays; break;
        case 1: current_draw = mobileglues_drawArrays; break;
        case 2: current_draw = krypton_drawArrays; break;
        case 3: current_draw = zink_drawArrays; break;
        default: current_draw = glDrawArrays; break;
    }
    LOGI("Renderer selected: %d", rendererId);
}

JNIEXPORT void JNICALL Java_com_nexus_nefu_NefuCoreEngine_nativeDrawArraysBatched
(JNIEnv* env, jobject thiz, jint mode, jintArray firsts, jintArray counts, jint renderer) {
    jsize len = env->GetArrayLength(firsts);
    jint *firstArr = env->GetIntArrayElements(firsts, nullptr);
    jint *countArr = env->GetIntArrayElements(counts, nullptr);

    draw_arrays_t drawFunc = current_draw;
    for (jsize i = 0; i < len; i++) {
        drawFunc(mode, firstArr[i], countArr[i]);
    }

    env->ReleaseIntArrayElements(firsts, firstArr, JNI_ABORT);
    env->ReleaseIntArrayElements(counts, countArr, JNI_ABORT);
}

} // extern "C"
