#include "global_jvm.hpp"
#include "jni-c-to-java.h"

#include <atomic>
#include <jni.h>
#include <pthread.h>
#include <rtc/rtc.h>

#define JNI_VERSION JNI_VERSION_1_6

static JavaVM* global_JVM;
static pthread_key_t thread_key;
static std::atomic<bool> jvm_unloading(false);

void detach_thread(void* value) {
    const auto jvm = static_cast<JavaVM*>(value);
    if (jvm != nullptr) {
        jvm->DetachCurrentThread();
    }
}

JNIEnv* get_jni_env_from_jvm(JavaVM* jvm) {
    JNIEnv* env;
    jint result = jvm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION);
    if (result == JNI_EDETACHED) {
        result = jvm->AttachCurrentThreadAsDaemon(reinterpret_cast<void**>(&env), nullptr);
        if (result == JNI_OK) {
            pthread_setspecific(thread_key, jvm);
        }
    }
    if (result != JNI_OK) {
        return nullptr;
    }
    return env;
}

JNIEnv* get_jni_env() {
    // make sure it's initialized
    if (global_JVM == nullptr || jvm_unloading) {
        return nullptr;
    }
    return get_jni_env_from_jvm(global_JVM);
}

void logger_callback(const rtcLogLevel level, const char* message) {
    if (message == nullptr) {
        return;
    }
    JNIEnv* env = get_jni_env();
    if (env != nullptr) {
        call_tel_schich_libdatachannel_LibDataChannel_log_cstr(env, level, message);
    }
}

#if defined(__linux__) && !defined(__GLIBC__) && !defined(__ANDROID__)
// musl gives new threads a fixed 128 KiB stack. The JVM reserves most of that for its own guard and
// shadow pages when a native thread attaches, so callbacks from the library's worker threads overflow
// before reaching Java. glibc sizes thread stacks from RLIMIT_STACK instead.
static constexpr size_t MIN_THREAD_STACK_SIZE = 1024 * 1024;

static void ensure_thread_stack_size() {
    pthread_attr_t attr;
    if (pthread_getattr_default_np(&attr) != 0) {
        return;
    }
    size_t size = 0;
    if (pthread_attr_getstacksize(&attr, &size) == 0 && size < MIN_THREAD_STACK_SIZE &&
        pthread_attr_setstacksize(&attr, MIN_THREAD_STACK_SIZE) == 0) {
        pthread_setattr_default_np(&attr);
    }
    pthread_attr_destroy(&attr);
}
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved) {
#if defined(__linux__) && !defined(__GLIBC__) && !defined(__ANDROID__)
    ensure_thread_stack_size();
#endif
    pthread_key_create(&thread_key, detach_thread);
    global_JVM = jvm;
    jvm_unloading.store(false);
    JNIEnv* env = get_jni_env_from_jvm(jvm);
    module_OnLoad(env);
    const auto level = static_cast<rtcLogLevel>(call_tel_schich_libdatachannel_LibDataChannel_initialNativeLogLevel(env));
    if (env->ExceptionCheck()) {
        return JNI_ERR;
    }
    rtcInitLogger(level, &logger_callback);
    rtcPreload();
    return JNI_VERSION;
}

extern "C" JNIEXPORT void JNICALL Java_tel_schich_libdatachannel_LibDataChannel_setLogLevelNative(
        JNIEnv* env, jclass clazz, const jint level) {
    rtcInitLogger(static_cast<rtcLogLevel>(level), &logger_callback);
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* jvm, void* reserved) {
    jvm_unloading.store(true);
    rtcCleanup();
    JNIEnv* env = get_jni_env();
    module_OnUnload(env);
    global_JVM = nullptr;
}
