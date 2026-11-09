#include "util.hpp"
#include <jni.h>
#include <rtc/rtc.h>
#include <stdint.h>
#include <stdlib.h>
#include <string>
#include <vector>

struct ice_mux {
    int listener;
    jobject owner;
    jmethodID dispatch;
};

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_listenerIdNative(
        JNIEnv* env, jclass clazz, const jlong handle) {
    return reinterpret_cast<ice_mux*>(static_cast<intptr_t>(handle))->listener;
}

static void RTC_API incoming_request(int listener, const rtcIceUdpMuxRequest* request, void* ptr) {
    const auto mux = static_cast<ice_mux*>(ptr);
    JNIEnv* env = get_jni_env();
    bool queued = false;
    if (env != nullptr && env->PushLocalFrame(4) == 0) {
        jstring local = env->NewStringUTF(request->localUfrag);
        jstring remote = !env->ExceptionCheck() ? env->NewStringUTF(request->remoteUfrag) : nullptr;
        jstring address = !env->ExceptionCheck() ? env->NewStringUTF(request->remoteAddress) : nullptr;
        if (!env->ExceptionCheck()) {
            queued = env->CallBooleanMethod(mux->owner, mux->dispatch, static_cast<jlong>(request->id), local, remote,
                                            address, static_cast<jint>(request->remotePort));
        }
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
            queued = false;
        }
        env->PopLocalFrame(nullptr);
    } else if (env != nullptr && env->ExceptionCheck()) {
        env->ExceptionClear();
    }
    if (!queued) {
        rtcRejectIceUdpMuxRequest(listener, request->id);
    }
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_setProxyProtocolNative(
        JNIEnv* env, jclass clazz, jstring address, const jint port, jobjectArray proxies) {
    const jsize count = env->GetArrayLength(proxies);
    std::vector<std::string> owned;
    owned.reserve(count);
    for (jsize i = 0; i < count; ++i) {
        auto entry = static_cast<jstring>(env->GetObjectArrayElement(proxies, i));
        const char* chars = entry != nullptr ? env->GetStringUTFChars(entry, nullptr) : nullptr;
        if (chars == nullptr) {
            if (entry != nullptr) {
                env->DeleteLocalRef(entry);
            }
            return RTC_ERR_INVALID;
        }
        owned.emplace_back(chars);
        env->ReleaseStringUTFChars(entry, chars);
        env->DeleteLocalRef(entry);
    }
    std::vector<const char*> entries;
    entries.reserve(owned.size());
    for (const auto& proxy : owned) {
        entries.push_back(proxy.c_str());
    }
    // A null address matches peers that leave theirs unset
    const char* host = address != nullptr ? env->GetStringUTFChars(address, nullptr) : nullptr;
    if (address != nullptr && host == nullptr) {
        return RTC_ERR_FAILURE;
    }
    const int result = rtcSetIceUdpMuxProxyProtocol(host, static_cast<uint16_t>(port), entries.data(),
                                                    static_cast<int>(entries.size()));
    if (host != nullptr) {
        env->ReleaseStringUTFChars(address, host);
    }
    return result;
}

extern "C" JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_proxyRelayNative(
        JNIEnv* env, jclass clazz, jstring address, const jint port, jstring client, const jint clientPort) {
    // A null address matches peers that leave theirs unset
    const char* host = address != nullptr ? env->GetStringUTFChars(address, nullptr) : nullptr;
    if (address != nullptr && host == nullptr) {
        return nullptr;
    }
    const char* peer = env->GetStringUTFChars(client, nullptr);
    if (peer == nullptr) {
        if (host != nullptr) {
            env->ReleaseStringUTFChars(address, host);
        }
        return nullptr;
    }
    char relay[64];
    const int result = rtcGetIceUdpMuxProxyRelay(host, static_cast<uint16_t>(port), peer,
                                                 static_cast<uint16_t>(clientPort), relay, sizeof(relay));
    env->ReleaseStringUTFChars(client, peer);
    if (host != nullptr) {
        env->ReleaseStringUTFChars(address, host);
    }
    // No relay speaks for the client, which is an answer rather than a failure
    if (result == RTC_ERR_NOT_AVAIL || wrap_error(env, "rtcGetIceUdpMuxProxyRelay", result) == EXCEPTION_THROWN) {
        return nullptr;
    }
    return env->NewStringUTF(relay);
}

extern "C" JNIEXPORT jlong JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_openNative(
        JNIEnv* env, jobject self, jstring address, const jint port, const jint maxPending, const jint timeoutMs) {
    const auto mux = static_cast<ice_mux*>(calloc(1, sizeof(ice_mux)));
    if (mux == nullptr) {
        return 0;
    }
    mux->listener = -1;
    mux->owner = env->NewGlobalRef(self);
    jclass clazz = !env->ExceptionCheck() ? env->GetObjectClass(self) : nullptr;
    mux->dispatch = clazz != nullptr
            ? env->GetMethodID(clazz, "dispatch", "(JLjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)Z")
            : nullptr;
    if (clazz != nullptr) {
        env->DeleteLocalRef(clazz);
    }
    // A null address binds every address, the way peers on the same port do
    const char* host = address != nullptr && !env->ExceptionCheck() ? env->GetStringUTFChars(address, nullptr) : nullptr;
    if ((address == nullptr || host != nullptr) && mux->owner != nullptr && mux->dispatch != nullptr) {
        rtcIceUdpMuxListenerConfiguration config = {};
        config.bindAddress = host;
        config.port = static_cast<uint16_t>(port);
        config.maxPendingRequests = static_cast<unsigned int>(maxPending);
        config.requestTimeoutMs = static_cast<unsigned int>(timeoutMs);
        mux->listener = rtcCreateIceUdpMuxListener(&config, incoming_request, mux);
    }
    if (host != nullptr) {
        env->ReleaseStringUTFChars(address, host);
    }
    if (mux->listener < 0) {
        if (mux->owner != nullptr) {
            env->DeleteGlobalRef(mux->owner);
        }
        free(mux);
        return 0;
    }
    return static_cast<jlong>(reinterpret_cast<intptr_t>(mux));
}

extern "C" JNIEXPORT void JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_closeNative(
        JNIEnv* env, jclass clazz, const jlong handle) {
    const auto mux = reinterpret_cast<ice_mux*>(static_cast<intptr_t>(handle));
    if (rtcDeleteIceUdpMuxListener(mux->listener) != RTC_ERR_SUCCESS) {
        throw_native_exception(env, "Failed to close ICE UDP mux listener");
        return;
    }
    // Native deletion waits for in-flight metadata callbacks before releasing this reference.
    env->DeleteGlobalRef(mux->owner);
    free(mux);
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_acceptNative(
        JNIEnv* env, jclass clazz, const jint listener, const jlong requestId, const jint peer) {
    return rtcAcceptIceUdpMuxPeer(listener, static_cast<uint64_t>(requestId), peer);
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_rejectNative(
        JNIEnv* env, jclass clazz, const jint listener, const jlong requestId) {
    return rtcRejectIceUdpMuxRequest(listener, static_cast<uint64_t>(requestId));
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_attachNative(
        JNIEnv* env, jclass clazz, const jint listener, const jlong requestId, const jint peer) {
    return rtcAttachIceUdpMuxPeer(listener, static_cast<uint64_t>(requestId), peer);
}

extern "C" JNIEXPORT jlongArray JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_statsNative(
        JNIEnv* env, jclass clazz, const jint listener) {
    rtcIceUdpMuxListenerStats stats;
    if (rtcGetIceUdpMuxListenerStats(listener, &stats) != RTC_ERR_SUCCESS) {
        throw_native_exception(env, "ICE UDP mux statistics unavailable");
        return nullptr;
    }
    const jlong values[] = {
            static_cast<jlong>(stats.received),
            static_cast<jlong>(stats.rejected),
            static_cast<jlong>(stats.agents),
            static_cast<jlong>(stats.mappedTuples),
            static_cast<jlong>(stats.pendingRequests),
            static_cast<jlong>(stats.notifications),
            static_cast<jlong>(stats.duplicates),
    };
    jlongArray result = env->NewLongArray(7);
    if (result != nullptr) {
        env->SetLongArrayRegion(result, 0, 7, values);
    }
    return result;
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_setAdmissionNative(
        JNIEnv* env, jclass clazz, const jint listener, jbyteArray context, jobjectArray prefixes,
        jobjectArray secrets) {
    if (context == nullptr) {
        return rtcSetIceUdpMuxAdmission(listener, nullptr);
    }
    const jsize count = env->GetArrayLength(prefixes);
    if (count > 8 || env->GetArrayLength(secrets) != count) {
        return RTC_ERR_INVALID;
    }
    std::vector<std::string> prefixCopies(count);
    std::vector<std::vector<char>> secretCopies(count);
    std::vector<rtcIceUdpMuxAdmissionKey> keys(count);
    // Failures break rather than return, so the secrets copied so far are still wiped
    int result = RTC_ERR_SUCCESS;
    for (jsize i = 0; i < count; ++i) {
        auto prefix = static_cast<jstring>(env->GetObjectArrayElement(prefixes, i));
        auto secret = static_cast<jbyteArray>(env->GetObjectArrayElement(secrets, i));
        if (prefix == nullptr || secret == nullptr) {
            result = RTC_ERR_INVALID;
            break;
        }
        const char* chars = env->GetStringUTFChars(prefix, nullptr);
        if (chars == nullptr) {
            result = RTC_ERR_FAILURE;
            break;
        }
        prefixCopies[i] = chars;
        env->ReleaseStringUTFChars(prefix, chars);
        secretCopies[i].resize(env->GetArrayLength(secret));
        env->GetByteArrayRegion(secret, 0, static_cast<jsize>(secretCopies[i].size()),
                                reinterpret_cast<jbyte*>(secretCopies[i].data()));
        env->DeleteLocalRef(prefix);
        env->DeleteLocalRef(secret);
        keys[i] = {prefixCopies[i].c_str(), secretCopies[i].data(), static_cast<int>(secretCopies[i].size())};
    }
    if (result == RTC_ERR_SUCCESS) {
        std::vector<char> contextCopy(env->GetArrayLength(context));
        env->GetByteArrayRegion(context, 0, static_cast<jsize>(contextCopy.size()),
                                reinterpret_cast<jbyte*>(contextCopy.data()));
        const rtcIceUdpMuxAdmission admission{contextCopy.data(), static_cast<int>(contextCopy.size()),
                                              keys.data(), static_cast<int>(count)};
        result = rtcSetIceUdpMuxAdmission(listener, &admission);
    }
    for (auto& secret : secretCopies) {
        volatile char* bytes = secret.data(); // Volatile, so the wipe survives optimization.
        for (size_t i = 0; i < secret.size(); ++i) bytes[i] = 0;
    }
    return result;
}
