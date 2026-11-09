package tel.schich.libdatachannel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Actual loopback UDP/JNI integration, with local IPv4 and IPv6 responders. */
public final class NativeStunMonitorProbe {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static int availablePort(InetAddress address) throws Exception {
        try (DatagramSocket socket = new DatagramSocket(new InetSocketAddress(address, 0))) {
            return socket.getLocalPort();
        }
    }

    private static void respond(DatagramSocket server, int expectedPort, boolean ipv6, int mappedPort,
                                boolean error) throws Exception {
        byte[] request = new byte[2048];
        DatagramPacket packet = new DatagramPacket(request, request.length);
        server.receive(packet);
        check(packet.getPort() == expectedPort, "STUN must use the listener's shared UDP port");
        ByteBuffer input = ByteBuffer.wrap(request);
        check(packet.getLength() >= 20 && input.getShort() == 1, "STUN Binding request");
        input.getShort();
        check(input.getInt() == 0x2112a442, "STUN cookie");
        byte[] transaction = new byte[12];
        input.get(transaction);
        ByteBuffer response = ByteBuffer.allocate(error ? 28 : ipv6 ? 44 : 32);
        response.putShort((short) (error ? 0x111 : 0x101));
        response.putShort((short) (response.capacity() - 20));
        response.putInt(0x2112a442).put(transaction);
        if (error) {
            response.putShort((short) 9).putShort((short) 4).putInt(0x00000500);
        } else {
            response.putShort((short) 0x20).putShort((short) (ipv6 ? 20 : 8));
            response.put((byte) 0).put((byte) (ipv6 ? 2 : 1)).putShort((short) (mappedPort ^ 0x2112));
            byte[] mapped = InetAddress.getByName(ipv6 ? "2001:db8::10" : "198.51.100.10").getAddress();
            for (int i = 0; i < mapped.length; ++i) response.put((byte) (mapped[i] ^ request[4 + i]));
        }
        byte[] bytes = response.array();
        server.send(new DatagramPacket(bytes, bytes.length, packet.getSocketAddress()));
    }

    private static StunBinding observed(StunUdpMuxMonitor monitor, long successes) throws Exception {
        for (int i = 0; i < 400; ++i) {
            StunBinding value = monitor.binding(0).orElseThrow();
            if (value.successfulResponses() >= successes) return value;
            Thread.sleep(5);
        }
        throw new AssertionError("STUN observation did not update");
    }

    private static void expectClosed(Runnable operation) {
        try { operation.run(); throw new AssertionError("closed resource accepted an operation"); }
        catch (IllegalStateException expected) { /* expected */ }
    }

    private static void family(String host) throws Exception {
        InetAddress address = InetAddress.getByName(host);
        boolean ipv6 = address.getAddress().length == 16;
        int port = availablePort(address);
        try (DatagramSocket server = new DatagramSocket(new InetSocketAddress(address, 0));
             IceUdpMuxListener listener = new IceUdpMuxListener(address, port, Runnable::run,
                 request -> { throw new AssertionError("discovery must not create an incoming peer"); });
             StunUdpMuxMonitor monitor = listener.monitorStun(host, server.getLocalPort())) {
            server.setSoTimeout(2000);
            StunBinding initial = monitor.binding(0).orElseThrow();
            check(initial.lastSuccessAge().isEmpty() && initial.mappedAddress().isEmpty(), "no invented initial mapping");
            check(initial.state() == StunBinding.State.PENDING, "initial transaction pending");
            check(monitor.binding(1).isEmpty(), "nonexistent server index");
            check(listener.statistics().agents() == 1 && listener.statistics().mappedTuples() == 0,
                "only discovery agent exists, no admitted peer");
            listener.close();
            expectClosed(() -> listener.monitorStun(host, server.getLocalPort()));
            respond(server, port, ipv6, 40000, false);
            StunBinding first = observed(monitor, 1);
            check(first.state() == StunBinding.State.SUCCEEDED && first.mappingRevision() == 1, "first observation");
            check(first.serverPort() == server.getLocalPort() && first.mappedPort() == 40000, "ports copied without truncation");
            check(InetAddress.getByName(first.serverAddress()).equals(address), "server address copied");
            long age = first.lastSuccessAge().orElseThrow().toMillis();
            Thread.sleep(25);
            check(first.lastSuccessAge().orElseThrow().toMillis() >= age + 20, "retained snapshots keep ageing");
            check(initial.successfulResponses() == 0 && initial.mappedAddress().isEmpty(),
                "new response cannot mutate an existing snapshot");

            AtomicBoolean stop = new AtomicBoolean();
            AtomicInteger reads = new AtomicInteger();
            AtomicReference<Throwable> failure = new AtomicReference<>();
            Thread reader = new Thread(() -> {
                try {
                    while (!stop.get()) {
                        try { monitor.binding(0); } catch (IllegalStateException closed) { break; }
                        reads.incrementAndGet();
                        Thread.yield();
                    }
                } catch (Throwable error) { failure.set(error); }
            }, "stun-monitor-reader");
            reader.setDaemon(true);
            reader.start();
            try {
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
                while (reads.get() < 20 && failure.get() == null && System.nanoTime() < deadline) Thread.yield();
                check(reads.get() >= 20, "reader made bounded progress");
                monitor.close();
            } finally {
                stop.set(true);
                reader.join(2000);
            }
            check(!reader.isAlive() && failure.get() == null, "concurrent Java read/close safe");
            monitor.close();
            expectClosed(() -> monitor.binding(0));
            check(first.mappedPort() == 40000 && first.lastSuccessAge().orElseThrow().toMillis() >= age + 20,
                "owned observation survives close and continues ageing");

            // Exercise the independent constructor and failed-state mapping.
            try (StunUdpMuxMonitor failed = new StunUdpMuxMonitor(address, port, host, server.getLocalPort())) {
                respond(server, port, ipv6, 0, true);
                StunBinding value = failed.binding(0).orElseThrow();
                for (int i = 0; i < 400 && value.failedTransactions() == 0; ++i) {
                    Thread.sleep(5);
                    value = failed.binding(0).orElseThrow();
                }
                check(value.state() == StunBinding.State.FAILED && value.failedTransactions() == 1,
                    "server failure copied");
                check(value.lastSuccessAge().isEmpty() && value.mappingRevision() == 0, "failure cannot invent freshness");
            }
        }
        System.out.println("STUN JNI " + host + ": shared socket, owned observations, ageing, failure and close PASS");
    }

    public static void main(String[] args) throws Exception {
        LibDataChannel.initialize();
        long peersBefore = LibDataChannelNative.rtcGetPeerConnectionCreationAttempts();
        family("127.0.0.1");
        family("::1");
        check(LibDataChannelNative.rtcGetPeerConnectionCreationAttempts() == peersBefore, "monitoring created no DTLS peer");
        System.out.println("STUN JNI observations PASS");
    }
}
