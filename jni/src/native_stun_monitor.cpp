#include "util.hpp"
#include <jni-c-to-java.h>
#include <algorithm>
#include <cstdint>
#include <limits>

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_StunUdpMuxMonitor_openNative(
        JNIEnv* env, jclass clazz, jstring address, jint localPort, jstring host, jint serverPort) {
    if (localPort < 1 || localPort > 65535 || serverPort < 1 || serverPort > 65535 || host == nullptr) {
        throw_native_exception(env, "Invalid STUN monitor host or port");
        return 0;
    }
    const char* bind = address != nullptr ? env->GetStringUTFChars(address, nullptr) : nullptr;
    if (env->ExceptionCheck()) return 0;
    const char* server = env->GetStringUTFChars(host, nullptr);
    int id = 0;
    if (server != nullptr) {
        rtcStunUdpMuxMonitorConfiguration config = {bind, static_cast<uint16_t>(localPort),
            server, static_cast<uint16_t>(serverPort)};
        id = rtcCreateStunUdpMuxMonitor(&config);
        env->ReleaseStringUTFChars(host, server);
    }
    if (bind != nullptr) env->ReleaseStringUTFChars(address, bind);
    if (!env->ExceptionCheck() && id <= 0) throw_native_exception(env, "Failed to create STUN UDP mux monitor");
    return id;
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_StunUdpMuxMonitor_openForListenerNative(
        JNIEnv* env, jclass clazz, jint listener, jstring host, jint port) {
    if (port < 1 || port > 65535 || host == nullptr) {
        throw_native_exception(env, "Invalid STUN monitor host or port");
        return 0;
    }
    const char* server = env->GetStringUTFChars(host, nullptr);
    if (server == nullptr) return 0;
    int id = rtcCreateIceUdpMuxStunMonitor(listener, server, static_cast<uint16_t>(port));
    env->ReleaseStringUTFChars(host, server);
    if (id <= 0) throw_native_exception(env, "Failed to create listener STUN monitor");
    return id;
}

extern "C" JNIEXPORT void JNICALL Java_tel_schich_libdatachannel_StunUdpMuxMonitor_closeNative(
        JNIEnv* env, jclass clazz, jint handle) {
    if (rtcDeleteStunUdpMuxMonitor(handle) != RTC_ERR_SUCCESS)
        throw_native_exception(env, "Failed to close STUN UDP mux monitor");
}

extern "C" JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_StunUdpMuxMonitor_bindingNative(
        JNIEnv* env, jclass clazz, jint handle, jint index, jlong sampledAtNanos) {
    if (index < 0) {
        throw_native_exception(env, "Invalid STUN server index");
        return nullptr;
    }
    rtcStunBinding binding{};
    int status = rtcGetStunUdpMuxBinding(handle, static_cast<unsigned int>(index), &binding);
    if (status == RTC_ERR_NOT_AVAIL) return nullptr;
    if (status != RTC_ERR_SUCCESS) {
        throw_native_exception(env, "STUN binding observation unavailable");
        return nullptr;
    }
    if (env->PushLocalFrame(4) != 0) return nullptr;
    jstring server = !env->ExceptionCheck() ? env->NewStringUTF(binding.serverAddress) : nullptr;
    jstring mapped = !env->ExceptionCheck() ? env->NewStringUTF(binding.mappedAddress) : nullptr;
    jobject result = nullptr;
    if (!env->ExceptionCheck()) {
        const jlong age = binding.lastSuccessAgeMs == std::numeric_limits<uint64_t>::max() ? -1 :
            static_cast<jlong>(std::min(binding.lastSuccessAgeMs, static_cast<uint64_t>(std::numeric_limits<jlong>::max())));
        result = call_tel_schich_libdatachannel_StunBinding_create(env, server, static_cast<jint>(binding.serverPort),
            mapped, static_cast<jint>(binding.mappedPort), static_cast<jint>(binding.state),
            static_cast<jlong>(binding.successfulResponses), static_cast<jlong>(binding.failedTransactions),
            static_cast<jlong>(binding.mappingRevision), age, sampledAtNanos);
    }
    return env->PopLocalFrame(result);
}
