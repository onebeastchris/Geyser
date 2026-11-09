package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.net.InetAddress;
import java.util.Objects;
import java.util.Optional;

/**
 * Owns native STUN discovery on a shared UDP socket without a remote ICE peer,
 * DTLS connection or data channel. Native code owns refresh scheduling.
 * Prefer {@link IceUdpMuxListener#monitorStun(String, int)} to inherit the exact
 * listener binding. Closing this monitor leaves other socket owners operational.
 */
public final class StunUdpMuxMonitor implements AutoCloseable {
    static { LibDataChannel.initialize(); }

    private int handle;

    /**
     * The bind address (including wildcard spelling) and fixed port must match
     * the existing mux owner exactly. Null selects libjuice's wildcard binding.
     * Server hostnames are resolved by native code once per monitor; numeric
     * addresses permit explicit per-family selection by the caller.
     */
    public StunUdpMuxMonitor(@Nullable InetAddress bindAddress, int localPort, String serverHost, int serverPort) {
        checkPort(localPort);
        checkServer(serverHost, serverPort);
        handle = openNative(Util.bindAddress(bindAddress),
            localPort, serverHost, serverPort);
        if (handle <= 0) throw new IllegalStateException("Failed to create STUN UDP mux monitor");
    }

    private StunUdpMuxMonitor(int handle) {
        if (handle <= 0) throw new IllegalStateException("Failed to create STUN UDP mux monitor");
        this.handle = handle;
    }

    static StunUdpMuxMonitor fromListener(int listener, String serverHost, int serverPort) {
        checkServer(serverHost, serverPort);
        return new StunUdpMuxMonitor(openForListenerNative(listener, serverHost, serverPort));
    }

    private static void checkPort(int port) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Explicit UDP port must be 1..65535");
    }

    private static void checkServer(String host, int port) {
        checkPort(port);
        Objects.requireNonNull(host, "serverHost");
        if (host.isEmpty() || host.length() > 255 || host.indexOf('\0') >= 0)
            throw new IllegalArgumentException("STUN server host must contain 1..255 characters without NUL");
    }

    /**
     * Copies one resolved-server observation atomically. Empty means the index
     * is not resolved/available; it does not distinguish resolving from failure.
     * Indices remain stable for this monitor. No packets are sent by this call.
     */
    public synchronized Optional<StunBinding> binding(int index) {
        if (index < 0) throw new IllegalArgumentException("STUN server index must be nonnegative");
        if (handle == 0) throw new IllegalStateException("STUN UDP mux monitor closed");
        return Optional.ofNullable(bindingNative(handle, index, System.nanoTime()));
    }

    @Override
    public synchronized void close() {
        if (handle == 0) return;
        closeNative(handle);
        handle = 0;
    }

    private static native int openNative(@Nullable String bindAddress, int localPort, String serverHost, int serverPort);
    private static native int openForListenerNative(int listener, String serverHost, int serverPort);
    private static native @Nullable StunBinding bindingNative(int handle, int index, long sampledAtNanos);
    private static native void closeNative(int handle);
}
