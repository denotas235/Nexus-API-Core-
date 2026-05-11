#include <jni.h>
#include <android/log.h>
#define LOG_TAG "NEFU"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C" {
    JNIEXPORT jboolean JNICALL Java_com_nexus_nefu_NefuCoreEngine_nativeInit(JNIEnv* env, jobject thiz) {
        LOGI("NEFU native core initialized.");
        return JNI_TRUE;
    }
    JNIEXPORT void JNICALL Java_com_nexus_nefu_NefuCoreEngine_nativeSelectRenderer(JNIEnv* env, jobject thiz, jint rendererId) {
        LOGI("Renderer selected: %d", rendererId);
    }
}
