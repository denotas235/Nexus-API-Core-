#include <jni.h>
#include <android/log.h>
#include <dlfcn.h>
#include <string>

#define TAG "NexusMaliCore"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

typedef unsigned int GLenum;
typedef unsigned char GLubyte;
typedef void* EGLDisplay;
typedef int EGLint;

struct GLPointers {
    void* lib;
    const GLubyte* (*glGetString)(GLenum);
};

struct EGLPointers {
    void* lib;
    EGLDisplay (*eglGetDisplay)(void*);
    const char* (*eglQueryString)(EGLDisplay, EGLint);
};

struct VulkanPointers {
    void* lib;
    void* (*vkEnumerateInstanceExtensionProperties)(const char*, unsigned int*, void*);
};

struct OpenALPointers {
    void* lib;
    void* (*alcGetString)(void*, int);
    void* (*alcOpenDevice)(const char*);
    int   (*alcCloseDevice)(void*);
};

static GLPointers     gl  = {};
static EGLPointers    egl = {};
static VulkanPointers vk  = {};
static OpenALPointers al  = {};
static void*          cl_lib = nullptr;
static bool initialized = false;

static void* loadLib(const char* name) {
    void* h = dlopen(name, RTLD_LAZY | RTLD_GLOBAL);
    if (!h) LOGE("dlopen(%s) failed: %s", name, dlerror());
    else LOGI("Loaded: %s", name);
    return h;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_initNexusCore(JNIEnv*, jclass) {
    if (initialized) return JNI_TRUE;
    LOGI("Initializing Nexus Mali Core...");

    gl.lib = loadLib("libGLESv2.so");
    if (gl.lib)
        gl.glGetString = (const GLubyte*(*)(GLenum))dlsym(gl.lib, "glGetString");

    egl.lib = loadLib("libEGL.so");
    if (egl.lib) {
        egl.eglGetDisplay  = (EGLDisplay(*)(void*))dlsym(egl.lib, "eglGetDisplay");
        egl.eglQueryString = (const char*(*)(EGLDisplay, EGLint))dlsym(egl.lib, "eglQueryString");
    }

    vk.lib = loadLib("libvulkan.so");
    if (vk.lib)
        vk.vkEnumerateInstanceExtensionProperties =
            (void*(*)(const char*, unsigned int*, void*))dlsym(vk.lib, "vkEnumerateInstanceExtensionProperties");

    cl_lib = loadLib("libOpenCL.so");

    al.lib = loadLib("libopenal.so");
    if (!al.lib) al.lib = loadLib("libopenal.so.1");
    if (al.lib) {
        al.alcGetString  = (void*(*)(void*, int))dlsym(al.lib, "alcGetString");
        al.alcOpenDevice = (void*(*)(const char*))dlsym(al.lib, "alcOpenDevice");
        al.alcCloseDevice= (int(*)(void*))dlsym(al.lib, "alcCloseDevice");
    }

    initialized = true;
    LOGI("Nexus Mali Core ready.");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_getGLExtensions(JNIEnv* env, jclass) {
    if (!initialized || !gl.glGetString) return env->NewStringUTF("");
    const GLubyte* ext = gl.glGetString(0x1F03); // GL_EXTENSIONS
    return env->NewStringUTF(ext ? (const char*)ext : "");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_getEGLExtensions(JNIEnv* env, jclass) {
    if (!initialized || !egl.eglGetDisplay || !egl.eglQueryString) return env->NewStringUTF("");
    EGLDisplay dpy = egl.eglGetDisplay(nullptr);
    const char* ext = egl.eglQueryString(dpy, 0x3055); // EGL_EXTENSIONS
    return env->NewStringUTF(ext ? ext : "");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_getVulkanExtensions(JNIEnv* env, jclass) {
    if (!initialized || !vk.vkEnumerateInstanceExtensionProperties)
        return env->NewStringUTF("");
    // Contar extensões
    unsigned int count = 0;
    vk.vkEnumerateInstanceExtensionProperties(nullptr, &count, nullptr);
    return env->NewStringUTF(count > 0 ? "Vulkan available" : "Vulkan not available");
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_getAudioExtensions(JNIEnv* env, jclass) {
    std::string result = "ANDROID_AAUDIO ANDROID_OPENSL_ES";
    if (al.lib && al.alcOpenDevice && al.alcGetString && al.alcCloseDevice) {
        void* dev = al.alcOpenDevice(nullptr);
        if (dev) {
            const char* alext = (const char*)al.alcGetString(dev, 0x1006); // ALC_EXTENSIONS
            if (alext) result += " " + std::string(alext);
            al.alcCloseDevice(dev);
        }
    }
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_nexuapicore_core_nativelink_NexusNativeLoader_shutdownNexusCore(JNIEnv*, jclass) {
    if (gl.lib)  dlclose(gl.lib);
    if (egl.lib) dlclose(egl.lib);
    if (vk.lib)  dlclose(vk.lib);
    if (cl_lib)  dlclose(cl_lib);
    if (al.lib)  dlclose(al.lib);
    initialized = false;
    LOGI("Nexus Mali Core shut down.");
}
