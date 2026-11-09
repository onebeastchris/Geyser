package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;
import tel.schich.jniaccess.JNIAccess;
import java.nio.file.Path;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcCreatePeerConnectionWithIdentity;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalDescriptionWithIce;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tel.schich.libdatachannel.LibDataChannelNative.rtcAddRemoteCandidate;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcAddTrack;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcAddTrackEx;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcClosePeerConnection;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcCreateDataChannelEx;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcCreatePeerConnection;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcDeletePeerConnection;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetLocalAddress;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetLocalDescription;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetLocalDescriptionType;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetMaxDataChannelStream;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetRemoteAddress;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetRemoteDescription;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetRemoteDescriptionType;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetRemoteMaxMessageSize;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcGetSelectedCandidatePair;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetDataChannelCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetGatheringStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetIceStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalCandidateCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalDescription;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetLocalDescriptionCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetRemoteDescription;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetSignalingStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetStateChangeCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcSetTrackCallback;
import static tel.schich.libdatachannel.LibDataChannelNative.setupPeerConnectionListener;
import static tel.schich.libdatachannel.Util.parseAddress;
import static tel.schich.libdatachannel.Util.wrapError;
import static tel.schich.libdatachannel.exception.LibDataChannelException.ERR_INVALID;

import java.io.Closeable;
import java.lang.ref.Cleaner;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PeerConnection implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PeerConnection.class);

    final int peerHandle;
    final CallbackDispatcher callbacks;
    private final ConcurrentMap<Integer, DataChannel> channels;
    private final ConcurrentMap<Integer, Track> tracks;
    private final Cleaner.Cleanable cleanable;
    private volatile boolean nativeTeardownComplete;
    private @Nullable CompletableFuture<Void> closeCompletion;
    private final Object preparationLock = new Object();
    private boolean preparationOwned, preparationCloseRequested;
    final PeerConnectionListener listener;

    public final EventListenerContainer<PeerConnectionCallback.LocalDescription> onLocalDescription;
    public final EventListenerContainer<PeerConnectionCallback.LocalCandidate> onLocalCandidate;
    public final EventListenerContainer<PeerConnectionCallback.StateChange> onStateChange;
    public final EventListenerContainer<PeerConnectionCallback.IceStateChange> onIceStateChange;
    public final EventListenerContainer<PeerConnectionCallback.GatheringStateChange> onGatheringStateChange;
    public final EventListenerContainer<PeerConnectionCallback.SignalingStateChange> onSignalingStateChange;
    public final EventListenerContainer<PeerConnectionCallback.DataChannel> onDataChannel;
    public final EventListenerContainer<PeerConnectionCallback.Track> onTrack;

    private PeerConnection(int peerHandle, CallbackDispatcher callbacks) {
        this.peerHandle = peerHandle;
        this.callbacks = callbacks;
        this.channels = new ConcurrentHashMap<>();
        this.tracks = new ConcurrentHashMap<>();
        this.listener = new PeerConnectionListener(this);

        this.onLocalDescription = new EventListenerContainer<>("LocalDescription", set -> rtcSetLocalDescriptionCallback(peerHandle, set), callbacks);
        this.onLocalCandidate = new EventListenerContainer<>("LocalCandidate", set -> rtcSetLocalCandidateCallback(peerHandle, set), callbacks);
        this.onStateChange = new EventListenerContainer<>("StateChange", set -> rtcSetStateChangeCallback(peerHandle, set), callbacks);
        this.onIceStateChange = new EventListenerContainer<>("IceStateChange", set -> rtcSetIceStateChangeCallback(peerHandle, set), callbacks);
        this.onGatheringStateChange = new EventListenerContainer<>("GatheringStateChange", set -> rtcSetGatheringStateChangeCallback(peerHandle, set), callbacks);
        this.onSignalingStateChange = new EventListenerContainer<>("SignalingStateChange", set -> rtcSetSignalingStateChangeCallback(peerHandle, set), callbacks);
        this.onDataChannel = new EventListenerContainer<>("DataChannel", set -> rtcSetDataChannelCallback(peerHandle, set), callbacks);
        this.onTrack = new EventListenerContainer<>("Track", set -> rtcSetTrackCallback(peerHandle, set), callbacks);

        this.cleanable = LibDataChannel.CLEANER.register(this, () -> {
            // make sure not to capture this here, that would be a memory leak
            if (rtcClosePeerConnection(peerHandle) != ERR_INVALID) {
                rtcDeletePeerConnection(peerHandle);
            }
        });
    }

    static PeerConnection fromNative(int handle, CallbackDispatcher callbacks) {
        PeerConnection peer = new PeerConnection(handle, callbacks);
        peer.preparationOwned = true;
        return peer;
    }

    boolean preparationCloseRequested() {
        synchronized (preparationLock) { return preparationCloseRequested; }
    }

    boolean releasePreparation() {
        synchronized (preparationLock) {
            if (preparationCloseRequested && !nativeTeardownComplete) return false;
            preparationOwned = false;
            return !preparationCloseRequested;
        }
    }

    void installNativeListener() {
        wrapError("setupPeerConnectionListener", setupPeerConnectionListener(peerHandle, listener));
    }

    static String @Nullable [] iceUrisToStrings(@Nullable Collection<URI> uris) {
        if (uris == null || uris.isEmpty()) {
            return null;
        }
        int index = 0;
        String[] strings = new String[uris.size()];
        for (URI server : uris) {
            strings[index++] = server.toASCIIString();
        }
        return strings;
    }

    /**
     * Creates a Peer Connection.
     *
     * Remember to {@link #close()} when done.
     *
     * @param config the peer configuration
     * @param executor the executor to run callbacks on
     * @return the peer connection
     */
    public static PeerConnection createPeer(PeerConnectionConfiguration config, Executor executor) {
        return createPeer(config, executor, null, null);
    }

    public static PeerConnection createPeer(PeerConnectionConfiguration config, Executor executor, DtlsIdentity identity) {
        Objects.requireNonNull(identity, "identity");
        return createPeer(config, executor, identity.certificate(), identity.privateKey(), identity.password());
    }

    /** Creates a peer using a paired PEM DTLS certificate/key, or the default identity if both null. */
    public static PeerConnection createPeer(PeerConnectionConfiguration config, Executor executor,
                                           @Nullable Path certificate, @Nullable Path key) {
        return createPeer(config, executor, certificate, key, null);
    }

    /** Imports an endpoint identity using the upstream native certificate configuration. */
    public static PeerConnection createPeer(PeerConnectionConfiguration config, Executor executor,
                                           @Nullable Path certificate, @Nullable Path key,
                                           @Nullable String keyPassword) {
        return createPeer(config, CallbackDispatcher.on(executor), certificate, key, keyPassword);
    }

    private static PeerConnection createPeer(PeerConnectionConfiguration config, CallbackDispatcher callbacks,
                                            @Nullable Path certificate, @Nullable Path key,
                                            @Nullable String keyPassword) {
        if ((certificate == null) != (key == null)) throw new IllegalArgumentException("Certificate/key must be paired");
        if (keyPassword != null && key == null) throw new IllegalArgumentException("A key password requires an identity");
        String proxyServer = null;
        if (config.proxyServer != null) {
            proxyServer = config.proxyServer.toASCIIString();
        }
        String bindAddress = Util.bindAddress(config.bindAddress);
        int result = rtcCreatePeerConnectionWithIdentity(
                iceUrisToStrings(config.iceServers),
                proxyServer,
                bindAddress,
                config.certificateType.state,
                config.iceTransportPolicy.state,
                config.enableIceTcp,
                config.enableIceUdpMux,
                config.iceUdpMuxDirect,
                config.disableAutoNegotiation,
                config.forceMediaTransport,
                config.portRangeBegin,
                config.portRangeEnd,
                config.mtu,
                config.maxMessageSize,
                certificate == null ? null : certificate.toString(),
                key == null ? null : key.toString(), keyPassword);

        final PeerConnection peer = new PeerConnection(wrapError("rtcCreatePeerConnection", result), callbacks);
        setupPeerConnectionListener(peer.peerHandle, peer.listener);

        return peer;
    }

    /** Diagnostic count at native peer construction, including failed attempts. */
    public static long nativeCreationAttempts() { return LibDataChannelNative.rtcGetPeerConnectionCreationAttempts(); }

    public static PeerConnection createPeer(PeerConnectionConfiguration config) {
        return createPeer(config, CallbackDispatcher.direct(), null, null, null);
    }

    @Nullable
    DataChannel channel(int channelHandle) {
        return channels.get(channelHandle);
    }

    DataChannel newChannel(int channelHandle) {
        return channels.computeIfAbsent(channelHandle, h -> new DataChannel(this, h, callbacks));
    }

    void dropChannelState(int channelHandle) {
        channels.remove(channelHandle);
    }

    Track newTrack(int trackHandle) {
        return tracks.computeIfAbsent(trackHandle, h -> new Track(this, h));
    }

    public void dropTrackState(int trackHandle) {
        tracks.remove(trackHandle);
    }

    /**
     * If it is not already closed, the Peer Connection is implicitly closed before being deleted. After this function has been called, pc must not be
     * used in a function call anymore. This function will block until all scheduled callbacks of pc return (except the one this function might be
     * called in) and no other callback will be called for pc after it returns.
     */
    @Override
    public void close() {
        // Native unregistration waits for in-flight arrivals before we drain their handles.
        onDataChannel.close();
        onTrack.close();
        try {
            closeChannels();
        } catch (Exception e) {
            LOGGER.warn("Failed to close channels of peer connection", e);
        }
        for (Track track : new ArrayList<>(tracks.values())) {
            track.close();
        }
        // Detach callbacks before the cleaner deletes their native peer handle.
        onLocalDescription.close();
        onLocalCandidate.close();
        onStateChange.close();
        onIceStateChange.close();
        onGatheringStateChange.close();
        onSignalingStateChange.close();
        boolean deferDeletion;
        synchronized (preparationLock) {
            deferDeletion = preparationOwned;
            if (deferDeletion) preparationCloseRequested = true;
        }
        if (deferDeletion) rtcClosePeerConnection(peerHandle);
        else cleanable.clean();
    }

    /**
     * Force-close and await native transport teardown before deleting the peer.
     * Call only from an external owner thread, never a native callback. A timeout
     * returns false and retains ownership so the caller can retry or fail closed.
     */
    public boolean closeAndAwait(java.time.Duration timeout) {
        if (EventListenerContainer.inCallback()) throw new IllegalStateException("Cannot await teardown from a native event callback");
        long millis = timeout.toMillis();
        if (millis < 1 || millis > 30_000) throw new IllegalArgumentException("Teardown timeout must be 1..30000 ms");
        try {
            closeAsync().toCompletableFuture().get(millis, TimeUnit.MILLISECONDS);
            return true;
        } catch (TimeoutException timeoutError) {
            return false;
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException error) {
            throw new IllegalStateException("Native close failed", error.getCause());
        }
    }

    /**
     * Starts native closure without blocking a worker on teardown. Safe from event
     * callbacks. Completion follows transport destruction and Java handle cleanup.
     * A peer still being initialized remains owned by its incoming-request handler.
     */
    public synchronized CompletionStage<Void> closeAsync() {
        if (closeCompletion == null) {
            closeCompletion = new CompletableFuture<>();
            int result = closeAsyncNative(peerHandle, this);
            if (result != 0) closeCompletion.completeExceptionally(
                new IllegalStateException("Cannot initiate native close: " + result));
        }
        return closeCompletion.minimalCompletionStage();
    }

    @JNIAccess
    private void nativeCloseCompleted() {
        nativeTeardownComplete = true;
        // Neither handle cleanup nor arbitrary user continuations run inside JNI completion.
        CompletableFuture.runAsync(() -> {
            CompletableFuture<Void> completion;
            synchronized (this) { completion = closeCompletion; }
            try {
                close();
                completion.complete(null);
            } catch (Throwable error) { completion.completeExceptionally(error); }
        });
    }

    private static native int closeAsyncNative(int peerHandle, PeerConnection owner);

    /**
     * Closes all Data Channels.
     */
    public void closeChannels() {
        for (final DataChannel ch : new ArrayList<>(this.channels.values())) {
            ch.close();
        }
        if (!this.channels.isEmpty()) {
            closeChannels();
        }
    }

    /** Installs the exact local ICE credentials before gathering starts. */
    public void setLocalDescription(@Nullable String type, String ufrag, String password) {
        if (ufrag.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("ICE credentials required");
        }
        wrapError("rtcSetLocalDescriptionWithIce", rtcSetLocalDescriptionWithIce(peerHandle, type, ufrag, password));
    }

    /**
     * Initiates the handshake process.
     * <p>
     * Following this call, the local description callback will be called with the local description, which must be sent to the remote peer by the
     * user's method of choice.
     * <p>
     * Note this call is implicit after {@link #setRemoteDescription} and {@link #createDataChannel} if
     * disableAutoNegotiation was not set on Peer Connection creation.
     *
     * @param type (optional): type of the description ("offer", "answer", "pranswer", or "rollback") or NULL for autodetection.
     */
    public void setLocalDescription(@Nullable String type) {
        wrapError("rtcSetLocalDescription", rtcSetLocalDescription(peerHandle, type));
    }

    /**
     * Retrieves the current local description in SDP format.
     *
     * @return the current local description
     */
    public String localDescription() {
        return rtcGetLocalDescription(peerHandle);
    }

    /**
     * Retrieves the current local description type as string. See {@link #setLocalDescription}.
     *
     * @return the current local description type
     */
    public String localDescriptionType() {
        return rtcGetLocalDescriptionType(peerHandle);
    }

    /**
     * Sets the remote description received from the remote peer by the user's method of choice. The remote description may have candidates or not.
     * <p>
     * If the remote description is an offer and disableAutoNegotiation was not set in rtcConfiguration, the library will automatically answer by
     * calling rtcSetLocalDescription internally. Otherwise, the user must call it to answer the remote description.
     *
     * @param sdp  the remote Session Description Protocol
     * @param type (optional): type of the description ("offer", "answer", "pranswer", or "rollback") or NULL for autodetection.
     */
    public void setRemoteDescription(String sdp, @Nullable SessionDescriptionType type) {
        Objects.requireNonNull(sdp, "sdp cannot be null");
        String typeString = null;
        if (type != null) {
            typeString = type.type;
        }
        wrapError("rtcSetRemoteDescription", rtcSetRemoteDescription(peerHandle, sdp, typeString));
    }

    /**
     * Retrieves the current remote description in SDP format.
     *
     * @return the current remote description
     */
    public String remoteDescription() {
        return rtcGetRemoteDescription(peerHandle);
    }


    /**
     * Retrieves the current remote description type as string.
     *
     * @return the current remote description type
     */
    @Nullable
    public SessionDescriptionType remoteDescriptionType() {
        return SessionDescriptionType.of(rtcGetRemoteDescriptionType(peerHandle));
    }

    /**
     * Adds a trickled remote candidate received from the remote peer by the user's method of choice.
     * The Peer Connection must have a remote description set.
     *
     * @param candidate a null-terminated SDP string representing the candidate (with or without the "a=" prefix)
     */
    public void addRemoteCandidate(String candidate) {
        addRemoteCandidate(candidate, null);
    }

    /**
     * Adds a trickled remote candidate received from the remote peer by the user's method of choice.
     * The Peer Connection must have a remote description set.
     *
     * @param candidate a null-terminated SDP string representing the candidate (with or without the "a=" prefix)
     * @param mediaId       (optional): a null-terminated string representing the mediaId of the candidate in the remote SDP description or NULL for
     *                  autodetection
     */
    public void addRemoteCandidate(String candidate, @Nullable String mediaId) {
        wrapError("rtcAddRemoteCandidate", rtcAddRemoteCandidate(peerHandle, candidate, mediaId));
    }

    /**
     * Retrieves the current local address, i.e. the network address of the currently selected local candidate. The address will have the format
     * "IP_ADDRESS:PORT", where IP_ADDRESS may be either IPv4 or IPv6. The call might fail if the PeerConnection is not in state RTC_CONNECTED, and
     * the address might change after connection.
     *
     * @return the local address
     */
    public InetSocketAddress localAddress() {
        return parseAddress(rtcGetLocalAddress(peerHandle));
    }

    /**
     * Retrieves the current remote address, i.e. the network address of the currently selected remote candidate. The address will have the format
     * "IP_ADDRESS:PORT", where IP_ADDRESS may be either IPv4 or IPv6. The call may fail if the state is not RTC_CONNECTED, and the address might
     * change after connection.
     *
     * @return the remote address
     */
    public InetSocketAddress remoteAddress() {
        return parseAddress(rtcGetRemoteAddress(peerHandle));
    }

    /**
     * Retrieves the currently selected candidate pair. The call may fail if the state is not RTC_CONNECTED, and the selected candidate pair might
     * change after connection.
     *
     * @return the selected candidate pair
     */
    public CandidatePair selectedCandidatePair() {
        return rtcGetSelectedCandidatePair(peerHandle);
    }


    /**
     * Retrieves the maximum stream ID a Data Channel may use. It is useful to create user-negotiated Data Channels with negotiated=true and
     * manualStream=true. The maximum is negotiated during connection, therefore the final value after connection might be lower than before
     * connection if the remote maximum is lower.
     *
     * @return maximum stream ID
     */
    public int maxDataChannelStream() {
        return rtcGetMaxDataChannelStream(peerHandle);
    }

    /**
     * Retrieves the maximum message size for data channels on the peer connection as negotiated with the remote peer.
     *
     * @return the maximum message size
     */
    public int remoteMaxMessageSize() {
        return rtcGetRemoteMaxMessageSize(peerHandle);
    }

    /**
     * Retrieves the round trip time of the underlying SCTP association.
     *
     * @return the round trip time, empty until the association reports one
     */
    public Optional<Duration> rtt() {
        final int millis = LibDataChannelNative.rtcGetRtt(peerHandle);
        if (millis < 0) {
            return Optional.empty();
        }
        return Optional.of(Duration.ofMillis(millis));
    }

    /**
     * Retrieves why this connection closed. Read it as soon as the state changes: it is recorded before the
     * change is reported, and the connection cannot be asked once it is deleted.
     *
     * @return the reason, {@link CloseReason#RTC_CLOSE_REASON_NONE} while the connection is open
     */
    public CloseReason closeReason() {
        final int reason = wrapError("rtcGetCloseReason", LibDataChannelNative.rtcGetCloseReason(peerHandle));
        final CloseReason known = CloseReason.of(reason);
        return known != null ? known : CloseReason.RTC_CLOSE_REASON_TRANSPORT_FAILED;
    }

    /**
     * Retrieves what the underlying SCTP association reports about itself.
     *
     * @return the association's stats, empty until it is connected
     */
    public Optional<SctpStats> sctpStats() {
        return Optional.ofNullable(LibDataChannelNative.rtcGetSctpStats(peerHandle));
    }

    public void setAnswer(String sdp) {
        this.setRemoteDescription(sdp, SessionDescriptionType.ANSWER);
    }


    /**
     * Adds a Data Channel on a Peer Connection. The Peer Connection does not need to be connected, however, the Data Channel will be open only when
     * the Peer Connection is connected.
     * <p>
     * rtcDataChannel() is equivalent to rtcDataChannelEx() with settings set to ordered, reliable, non-negotiated, with automatic stream ID selection
     * (all flags set to false), and protocol set to an empty string.
     *
     * @param label a user-defined UTF-8 string representing the Data Channel name
     * @return the created data channel
     */
    public DataChannel createDataChannel(String label) {
        return createDataChannel(label, DataChannelInitSettings.DEFAULT);
    }

    /**
     * Adds a Data Channel on a Peer Connection. The Peer Connection does not need to be connected, however, the Data Channel will be open only when
     * the Peer Connection is connected.
     *
     * @param label a user-defined UTF-8 string representing the Data Channel name
     * @param init  a structure of initialization settings
     * @return the created data channel
     */
    public DataChannel createDataChannel(String label, DataChannelInitSettings init) {
        final DataChannelReliability reliability = init.reliability();
        int stream = init.stream().orElse(0);
        boolean manualStream = init.stream().isPresent();
        final int channelHandle = wrapError("rtcCreateDataChannelEx", rtcCreateDataChannelEx(peerHandle, label, reliability.isUnordered(), reliability.isUnreliable(), reliability.maxPacketLifeTime().toMillis(), reliability.maxRetransmits(), init.protocol().orElse(null), init.isNegotiated(), stream, manualStream));
        return newChannel(channelHandle);
    }

    // Adds a new Track on a Peer Connection. The Peer Connection does not need to be connected, however, the Track will be open only when the Peer Connection is connected.
    // sdp: a null-terminated string specifying the corresponding media SDP. It must start with a m-line and include a mid parameter.
    public Track addTrack(String sdp) {
        final int trackHandle = wrapError("rtcAddTrack", rtcAddTrack(peerHandle, sdp));
        return newTrack(trackHandle);
    }

    public Track addTrack(TrackInit init) {
        final int trackHandle = wrapError("rtcAddTrackEx", rtcAddTrackEx(peerHandle, init.direction().direction, init.codec().codec));
        return newTrack(trackHandle);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PeerConnection)) return false;
        PeerConnection that = (PeerConnection) o;
        return peerHandle == that.peerHandle;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerHandle);
    }
}
