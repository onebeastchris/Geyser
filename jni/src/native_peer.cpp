#include "callback.hpp"
#include "util.hpp"
#include "candidate_pair_buffer.hpp"
#include <jni-c-to-java.h>
#include <jni-java-to-c.h>
#include <jni.h>
#include <rtc/rtc.h>
#include <cstdint>
#include <cstdlib>

void RTC_API handle_local_description(int pc, const char* sdp, const char* type, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onLocalDescription_cstr, sdp, type);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetLocalDescriptionCallback, handle_local_description)

void RTC_API handle_local_candidate(int pc, const char* candidate, const char* mediaId, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onLocalCandidate_cstr, candidate, mediaId);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetLocalCandidateCallback, handle_local_candidate)

void RTC_API handle_state_change(int pc, const rtcState state, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onStateChange, state);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetStateChangeCallback, handle_state_change)

void RTC_API handle_ice_state_change(int pc, const rtcIceState state, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onIceStateChange, state);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetIceStateChangeCallback, handle_ice_state_change)

void RTC_API handle_gathering_state_change(int pc, const rtcGatheringState state, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onGatheringStateChange, state);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetGatheringStateChangeCallback, handle_gathering_state_change)

void RTC_API handle_signaling_state_change(int pc, const rtcSignalingState state, void* ptr) {
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onSignalingStateChange, state);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetSignalingStateChangeCallback, handle_signaling_state_change)

void RTC_API handle_data_channel(int pc, const int channelHandle, void* ptr) {
    rtcSetUserPointer(channelHandle, ptr);
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onDataChannel, channelHandle);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetDataChannelCallback, handle_data_channel)

void RTC_API handle_track(int pc, const int trackHandle, void* ptr) {
    rtcSetUserPointer(trackHandle, ptr);
    DISPATCH_JNI(call_tel_schich_libdatachannel_PeerConnectionListener_onTrack, trackHandle);
}
SET_CALLBACK_INTERFACE_IMPL(rtcSetTrackCallback, handle_track)

struct incoming_peer {
    int listener;
    uint64_t request_id;
    const char* remote_sdp;
    rtcLocalDescriptionInit local_description;
    int pc;
};

static jint create_peer(JNIEnv* env, jclass clazz,
                        jobjectArray iceServers, jstring proxyServer,
                        jstring bindAddress, const jint certificateType,
                        const jint iceTransportPolicy,
                        const jboolean enableIceTcp,
                        const jboolean enableIceUdpMux,
                        const jboolean iceUdpMuxDirect,
                        const jboolean disableAutoNegotiation,
                        const jboolean forceMediaTransport,
                        const jint portRangeBegin, const jint portRangeEnd,
                        const jint mtu, const jint maxMessageSize,
                        jstring certificateFile, jstring keyFile, jstring keyPassword,
                        incoming_peer* incoming) {
    // Field by field so that added configuration fields keep compiling.
    rtcConfiguration config = {};
    config.certificateType = static_cast<rtcCertificateType>(certificateType);
    config.iceTransportPolicy = static_cast<rtcTransportPolicy>(iceTransportPolicy);
    config.enableIceTcp = static_cast<bool>(enableIceTcp);
    config.enableIceUdpMux = static_cast<bool>(enableIceUdpMux);
    config.iceUdpMuxDirect = static_cast<bool>(iceUdpMuxDirect);
    config.disableAutoNegotiation = static_cast<bool>(disableAutoNegotiation);
    config.forceMediaTransport = static_cast<bool>(forceMediaTransport);
    config.portRangeBegin = portRangeBegin;
    config.portRangeEnd = portRangeEnd;
    config.mtu = mtu;
    config.maxMessageSize = maxMessageSize;

    jstring* serverStrings = nullptr;

    if (iceServers != nullptr) {
        config.iceServersCount = env->GetArrayLength(iceServers);
        if (config.iceServersCount > 0) {
            config.iceServers = static_cast<const char**>(malloc(sizeof(char*) * config.iceServersCount));
            serverStrings = static_cast<jstring*>(malloc(sizeof(jstring) * config.iceServersCount));
            if (config.iceServers == nullptr || serverStrings == nullptr) {
                free(config.iceServers);
                free(serverStrings);
                THROW_FAILED_MALLOC(env, config.iceServers);
                return EXCEPTION_THROWN;
            }

            for (int i = 0; i < config.iceServersCount; i++) {
                serverStrings[i] = reinterpret_cast<jstring>(env->GetObjectArrayElement(iceServers, i));// we need a reference to release later
                config.iceServers[i] = env->GetStringUTFChars(serverStrings[i], nullptr);
                if (config.iceServers[i] == nullptr) {
                    // release everything and throw an exception
                    for (int j = 0; j < i; j++) {
                        env->ReleaseStringUTFChars(serverStrings[j], config.iceServers[j]);
                    }
                    free(config.iceServers);
                    free(serverStrings);
                    throw_native_exception(env, "Failed to get ice server string!");
                    return EXCEPTION_THROWN;
                }
            }
        }
    }

    if (proxyServer != nullptr) {
        config.proxyServer = env->GetStringUTFChars(proxyServer, nullptr);
    }
    if (bindAddress != nullptr) {
        config.bindAddress = env->GetStringUTFChars(bindAddress, nullptr);
    }

    const char* certificate = certificateFile != nullptr ? env->GetStringUTFChars(certificateFile, nullptr) : nullptr;
    const char* key = keyFile != nullptr && !env->ExceptionCheck() ? env->GetStringUTFChars(keyFile, nullptr) : nullptr;
    const char* pass = keyPassword != nullptr && !env->ExceptionCheck() ? env->GetStringUTFChars(keyPassword, nullptr) : nullptr;
    config.certificatePemFile = certificate;
    config.keyPemFile = key;
    config.keyPemPass = pass;

    jint result = EXCEPTION_THROWN;
    if (!env->ExceptionCheck()) {
        if (incoming != nullptr) {
            result = rtcPrepareIceUdpMuxPeer(incoming->listener, incoming->request_id, &config, incoming->remote_sdp,
                                             &incoming->local_description, &incoming->pc);
        } else {
            result = static_cast<jint>(rtcCreatePeerConnection(&config));
        }
    }

    if (pass != nullptr) {
        env->ReleaseStringUTFChars(keyPassword, pass);
    }
    if (certificate != nullptr) {
        env->ReleaseStringUTFChars(certificateFile, certificate);
    }
    if (key != nullptr) {
        env->ReleaseStringUTFChars(keyFile, key);
    }

    if (proxyServer != nullptr) {
        env->ReleaseStringUTFChars(proxyServer, config.proxyServer);
    }
    if (bindAddress != nullptr) {
        env->ReleaseStringUTFChars(bindAddress, config.bindAddress);
    }

    if (config.iceServers != nullptr && config.iceServersCount > 0) {
        if (serverStrings != nullptr) {
            for (int i = 0; i < config.iceServersCount; i++) {
                env->ReleaseStringUTFChars(serverStrings[i], config.iceServers[i]);
            }
        }
        free(config.iceServers);
        free(serverStrings);
    }

    return result;
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcCreatePeerConnection(JNIEnv* env, jclass clazz,
                                                                            jobjectArray iceServers, jstring proxyServer,
                                                                            jstring bindAddress, const jint certificateType,
                                                                            const jint iceTransportPolicy,
                                                                            const jboolean enableIceTcp,
                                                                            const jboolean enableIceUdpMux,
                                                                            const jboolean iceUdpMuxDirect,
                                                                            const jboolean disableAutoNegotiation,
                                                                            const jboolean forceMediaTransport,
                                                                            const jint portRangeBegin, const jint portRangeEnd,
                                                                            const jint mtu, const jint maxMessageSize) {
    return create_peer(env, clazz, iceServers, proxyServer, bindAddress, certificateType, iceTransportPolicy,
                       enableIceTcp, enableIceUdpMux, iceUdpMuxDirect, disableAutoNegotiation, forceMediaTransport, portRangeBegin,
                       portRangeEnd, mtu, maxMessageSize, nullptr, nullptr, nullptr, nullptr);
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcCreatePeerConnectionWithIdentity(JNIEnv* env, jclass clazz,
                                                                            jobjectArray iceServers, jstring proxyServer,
                                                                            jstring bindAddress, const jint certificateType,
                                                                            const jint iceTransportPolicy,
                                                                            const jboolean enableIceTcp,
                                                                            const jboolean enableIceUdpMux,
                                                                            const jboolean iceUdpMuxDirect,
                                                                            const jboolean disableAutoNegotiation,
                                                                            const jboolean forceMediaTransport,
                                                                            const jint portRangeBegin, const jint portRangeEnd,
                                                                            const jint mtu, const jint maxMessageSize,
                                                                            jstring certificateFile, jstring keyFile,
                                                                            jstring keyPassword) {
    return create_peer(env, clazz, iceServers, proxyServer, bindAddress, certificateType, iceTransportPolicy,
                       enableIceTcp, enableIceUdpMux, iceUdpMuxDirect, disableAutoNegotiation, forceMediaTransport, portRangeBegin,
                       portRangeEnd, mtu, maxMessageSize, certificateFile, keyFile, keyPassword, nullptr);
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcClosePeerConnection(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return rtcClosePeerConnection(peerHandle);
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcDeletePeerConnection(JNIEnv* env, jclass clazz,
                                                                            const jint peerHandle) {
    void* callback = rtcGetUserPointer(peerHandle);
    const jint result = rtcDeletePeerConnection(peerHandle);
    if (result == RTC_ERR_SUCCESS) {
        release_callback(callback);
    }

    return result;
}


JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetLocalDescription(JNIEnv* env, jclass clazz, const jint peerHandle, jstring type) {
    const char* c_type;
    if (type == nullptr) {
        c_type = nullptr;
    } else {
        c_type = env->GetStringUTFChars(type, nullptr);
        if (c_type == nullptr) {
            THROW_FAILED_GET_STR(env, type);
            return EXCEPTION_THROWN;
        }
    }
    const int result = WRAP_ERROR(env, rtcSetLocalDescription(peerHandle, c_type));
    env->ReleaseStringUTFChars(type, c_type);
    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalDescription(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetLocalDescription, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalDescriptionType(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetLocalDescriptionType, peerHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetRemoteDescription(JNIEnv* env, jclass clazz, const jint peerHandle, jstring sdp, jstring type) {
    const char* c_sdp = env->GetStringUTFChars(sdp, nullptr);
    if (c_sdp == nullptr) {
        THROW_FAILED_GET_STR(env, sdp);
        return EXCEPTION_THROWN;
    }
    const char* c_type;
    if (type == nullptr) {
        c_type = nullptr;
    } else {
        c_type = env->GetStringUTFChars(type, nullptr);
        if (c_type == nullptr) {
            env->ReleaseStringUTFChars(sdp, c_sdp);
            THROW_FAILED_GET_STR(env, type);
            return EXCEPTION_THROWN;
        }
    }
    const int result = WRAP_ERROR(env, rtcSetRemoteDescription(peerHandle, c_sdp, c_type));
    env->ReleaseStringUTFChars(sdp, c_sdp);
    if (c_type != nullptr) {
        env->ReleaseStringUTFChars(type, c_type);
    }
    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteDescription(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetRemoteDescription, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteDescriptionType(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetRemoteDescriptionType, peerHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcAddRemoteCandidate(JNIEnv* env, jclass clazz, const jint peerHandle, jstring candidate, jstring mediaId) {
    const char* c_candidate = nullptr;
    if (candidate != nullptr) {
        c_candidate = env->GetStringUTFChars(candidate, nullptr);
        if (c_candidate == nullptr) {
            THROW_FAILED_GET_STR(env, candidate);
            return EXCEPTION_THROWN;
        }
    }
    const char* c_mediaId = nullptr;
    if (mediaId != nullptr) {
        c_mediaId = env->GetStringUTFChars(mediaId, nullptr);
        if (c_mediaId == nullptr) {
            if (c_candidate != nullptr) {
                env->ReleaseStringUTFChars(candidate, c_candidate);
            }
            THROW_FAILED_GET_STR(env, mediaId);
            return EXCEPTION_THROWN;
        }
    }

    const int result = rtcAddRemoteCandidate(peerHandle, c_candidate, c_mediaId);
    if (c_candidate != nullptr) {
        env->ReleaseStringUTFChars(candidate, c_candidate);
    }
    if (c_mediaId != nullptr) {
        env->ReleaseStringUTFChars(mediaId, c_mediaId);
    }

    return result;
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetLocalAddress(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetLocalAddress, peerHandle);
}

JNIEXPORT jstring JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRemoteAddress(JNIEnv* env, jclass clazz, const jint peerHandle) {
    return GET_DYNAMIC_STRING(env, rtcGetRemoteAddress, peerHandle);
}

JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetSelectedCandidatePair(JNIEnv* env, jclass clazz, const jint peerHandle) {
    try {
        std::string local, remote;
        const int result = candidate_pair_buffer::read(peerHandle, local, remote, rtcGetSelectedCandidatePair);
        if (result < 0) {
            WRAP_ERROR(env, result);
            return nullptr;
        }
        return call_tel_schich_libdatachannel_CandidatePair_parse_cstr(env, local.c_str(), remote.c_str());
    } catch (const std::bad_alloc &) {
        throw_native_exception(env, "Failed to allocate selected candidate pair buffers");
        return nullptr;
    }
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_setupPeerConnectionListener(JNIEnv* env, jclass clazz, const jint peerHandle, jobject listener) {
    void* callback = register_callback(env, listener);
    if (callback == nullptr) {
        throw_native_exception(env, "Failed to allocate callback for PeerConnectionListener");
        return EXCEPTION_THROWN;
    }
    rtcSetUserPointer(peerHandle, callback);

    return RTC_ERR_SUCCESS;
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetRtt(JNIEnv* env, jclass clazz,
                                                                                     const jint peerHandle) {
    return rtcGetRtt(peerHandle);
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetCloseReason(JNIEnv* env, jclass clazz,
                                                                                             const jint peerHandle) {
    return rtcGetCloseReason(peerHandle);
}

JNIEXPORT jobject JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetSctpStats(JNIEnv* env,
                                                                                             jclass clazz,
                                                                                             const jint peerHandle) {
    rtcSctpStats stats{};
    const int status = rtcGetSctpStats(peerHandle, &stats);
    // Not connected yet is an answer, a bad handle is not
    if (status == RTC_ERR_NOT_AVAIL || wrap_error(env, "rtcGetSctpStats", status) == EXCEPTION_THROWN) {
        return nullptr;
    }
    return call_tel_schich_libdatachannel_SctpStats_create(env, stats.rtt, stats.rto,
        static_cast<jlong>(stats.congestionWindow), static_cast<jlong>(stats.peerReceiveWindow),
        static_cast<jlong>(stats.unackedChunks), static_cast<jlong>(stats.pendingChunks),
        static_cast<jlong>(stats.dataTimeouts));
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcSetLocalDescriptionWithIce(
        JNIEnv* env, jclass clazz, const jint peer, jstring type, jstring ufrag, jstring password) {
    const char* c_type = type != nullptr ? env->GetStringUTFChars(type, nullptr) : nullptr;
    if (type != nullptr && c_type == nullptr) {
        return EXCEPTION_THROWN;
    }
    const char* c_ufrag = env->GetStringUTFChars(ufrag, nullptr);
    const char* c_password = c_ufrag != nullptr ? env->GetStringUTFChars(password, nullptr) : nullptr;
    rtcLocalDescriptionInit init = {};
    init.iceUfrag = c_ufrag;
    init.icePwd = c_password;
    const jint result = c_password != nullptr ? rtcSetLocalDescriptionEx(peer, c_type, &init) : EXCEPTION_THROWN;
    if (c_password != nullptr) {
        env->ReleaseStringUTFChars(password, c_password);
    }
    if (c_ufrag != nullptr) {
        env->ReleaseStringUTFChars(ufrag, c_ufrag);
    }
    if (c_type != nullptr) {
        env->ReleaseStringUTFChars(type, c_type);
    }
    return result;
}

JNIEXPORT jlong JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_rtcGetPeerConnectionCreationAttempts(
        JNIEnv* env, jclass clazz) {
#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
    return static_cast<jlong>(rtcGetPeerConnectionCreationAttempts());
#else
    throw_native_exception(env, "Native construction diagnostics require a test build");
    return -1;
#endif
}

JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_LibDataChannelNative_setTestDispatchDelay(
        JNIEnv* env, jclass clazz, const jint millis) {
#ifdef RTC_ENABLE_TEST_DIAGNOSTICS
    return set_test_dispatch_delay(millis);
#else
    throw_native_exception(env, "Native dispatch delays require a test build");
    return -1;
#endif
}

struct peer_close_observer {
    jobject peer;
    jmethodID completed;
};

static void RTC_API peer_close_completed(int pc, void* ptr) {
    const auto observer = static_cast<peer_close_observer*>(ptr);
    JNIEnv* env = get_jni_env();
    if (env != nullptr) {
        env->CallVoidMethod(observer->peer, observer->completed);
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        env->DeleteGlobalRef(observer->peer);
    }
    free(observer);
}

extern "C" JNIEXPORT jint JNICALL Java_tel_schich_libdatachannel_PeerConnection_closeAsyncNative(
        JNIEnv* env, jclass clazz, const jint pc, jobject peer) {
    const auto observer = static_cast<peer_close_observer*>(calloc(1, sizeof(peer_close_observer)));
    if (observer == nullptr) {
        return RTC_ERR_FAILURE;
    }
    observer->peer = env->NewGlobalRef(peer);
    jclass type = observer->peer != nullptr ? env->GetObjectClass(peer) : nullptr;
    observer->completed = type != nullptr ? env->GetMethodID(type, "nativeCloseCompleted", "()V") : nullptr;
    if (type != nullptr) {
        env->DeleteLocalRef(type);
    }
    int result = RTC_ERR_FAILURE;
    if (observer->peer != nullptr && observer->completed != nullptr && !env->ExceptionCheck()) {
        result = rtcClosePeerConnectionAsync(pc, peer_close_completed, observer);
    }
    if (result != RTC_ERR_SUCCESS) {
        if (observer->peer != nullptr) {
            env->DeleteGlobalRef(observer->peer);
        }
        free(observer);
    }
    return result;
}

JNIEXPORT jint JNICALL
Java_tel_schich_libdatachannel_LibDataChannelNative_rtcClosePeerConnectionAndWait(JNIEnv* env, jclass clazz,
                                                                                  const jint peerHandle,
                                                                                  const jint timeoutMs) {
    return rtcClosePeerConnectionAndWait(peerHandle, timeoutMs);
}

extern "C" JNIEXPORT jintArray JNICALL Java_tel_schich_libdatachannel_IceUdpMuxListener_prepareConfiguredNative(
        JNIEnv* env, jclass clazz, const jint listener, const jlong requestId,
        jobjectArray iceServers, jstring proxyServer, jstring bindAddress, const jint certificateType,
        const jint iceTransportPolicy, const jboolean enableIceTcp, const jboolean enableIceUdpMux,
        const jboolean disableAutoNegotiation, const jboolean forceMediaTransport,
        const jint portRangeBegin, const jint portRangeEnd, const jint mtu, const jint maxMessageSize,
        jstring certificateFile, jstring keyFile, jstring keyPassword,
        jstring remoteDescription, jstring localUfrag, jstring localPassword) {
    // Allocate the result before creating anything: ownership must never be lost on allocation failure.
    jintArray result = env->NewIntArray(2);
    if (result == nullptr) {
        return nullptr;
    }
    incoming_peer incoming = {};
    incoming.listener = listener;
    incoming.request_id = static_cast<uint64_t>(requestId);
    incoming.pc = -1;
    incoming.remote_sdp = env->GetStringUTFChars(remoteDescription, nullptr);
    incoming.local_description.iceUfrag = !env->ExceptionCheck() ? env->GetStringUTFChars(localUfrag, nullptr) : nullptr;
    incoming.local_description.icePwd = !env->ExceptionCheck() ? env->GetStringUTFChars(localPassword, nullptr) : nullptr;
    jint status = EXCEPTION_THROWN;
    if (!env->ExceptionCheck()) {
        // Prepared peers are admitted through the listener, never direct.
        status = create_peer(env, clazz, iceServers, proxyServer, bindAddress, certificateType, iceTransportPolicy,
                             enableIceTcp, enableIceUdpMux, JNI_FALSE, disableAutoNegotiation, forceMediaTransport, portRangeBegin,
                             portRangeEnd, mtu, maxMessageSize, certificateFile, keyFile, keyPassword, &incoming);
    }
    if (incoming.remote_sdp != nullptr) {
        env->ReleaseStringUTFChars(remoteDescription, incoming.remote_sdp);
    }
    if (incoming.local_description.iceUfrag != nullptr) {
        env->ReleaseStringUTFChars(localUfrag, incoming.local_description.iceUfrag);
    }
    if (incoming.local_description.icePwd != nullptr) {
        env->ReleaseStringUTFChars(localPassword, incoming.local_description.icePwd);
    }
    if (!env->ExceptionCheck()) {
        const jint values[] = {status, incoming.pc};
        env->SetIntArrayRegion(result, 0, 2, values);
    }
    return result;
}
