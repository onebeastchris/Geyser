package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tel.schich.libdatachannel.LibDataChannelNative.rtcDeletePeerConnection;

/**
 * Deleting a peer connection blocks until libdatachannel has drained the callbacks it already scheduled. The callback
 * state behind the peer's user pointer therefore has to outlive that call, otherwise a draining callback reads memory
 * that has already been freed.
 *
 * <p>A use after free takes the whole JVM down, so the scenario runs in a child process and this test only looks at how
 * that process ended.
 */
class PeerCallbackLifecycleTest {
    @Test
    @Timeout(300)
    void peerDeletionKeepsCallbackStateUntilScheduledCallbacksReturn() throws Exception {
        String java = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        Process process = new ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), Churn.class.getName())
                .redirectErrorStream(true)
                .start();

        String output;
        try (InputStream stdout = process.getInputStream()) {
            output = readFully(stdout);
        }
        process.waitFor();

        assertEquals(0, process.exitValue(), "the churn process died, its output was:\n" + output);
    }

    private static String readFully(InputStream stream) throws IOException {
        byte[] buffer = new byte[8192];
        StringBuilder text = new StringBuilder();
        int read;
        while ((read = stream.read(buffer)) != -1) {
            text.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
        }
        return text.toString();
    }

    /**
     * Repeatedly parks a peer connection inside one of its callbacks, deletes the peer from another thread and then
     * lets the callback return, so that the callbacks queued behind it are delivered while the deletion is waiting.
     */
    public static final class Churn {
        private static final int ITERATIONS = 100;
        private static final long TIMEOUT_SECONDS = 10;

        public static void main(String[] args) throws Exception {
            // holding on to the peers keeps their cleaners from deleting handles that have since been reused
            List<PeerConnection> peers = new ArrayList<>();
            for (int i = 0; i < ITERATIONS; i++) {
                peers.add(deleteWhileCallbackIsRunning());
            }
            System.out.println("survived " + ITERATIONS + " deletions, kept " + peers.size() + " peers");
        }

        private static PeerConnection deleteWhileCallbackIsRunning() throws Exception {
            CountDownLatch callbackEntered = new CountDownLatch(1);
            CountDownLatch releaseCallback = new CountDownLatch(1);

            PeerConnectionConfiguration config = PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);
            PeerConnection peer = PeerConnection.createPeer(config);
            peer.onSignalingStateChange.register((ignoredPeer, ignoredState) -> {
                callbackEntered.countDown();
                await(releaseCallback);
            });
            // these queue up behind the parked callback and are drained while the deletion waits
            peer.onLocalDescription.register((ignoredPeer, ignoredSdp, ignoredType) -> {});
            peer.onLocalCandidate.register((ignoredPeer, ignoredCandidate, ignoredMediaId) -> {});
            peer.onGatheringStateChange.register((ignoredPeer, ignoredState) -> {});
            peer.onStateChange.register((ignoredPeer, ignoredState) -> {});
            peer.createDataChannel("callback-lifecycle");
            peer.setLocalDescription("offer");

            if (!callbackEntered.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("the signaling state callback never ran");
            }

            Thread deletion = new Thread(() -> rtcDeletePeerConnection(peer.peerHandle), "peer-deletion");
            deletion.start();
            // let the deletion reach the point where it waits for the scheduled callbacks
            Thread.sleep(50);
            releaseCallback.countDown();
            deletion.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            if (deletion.isAlive()) {
                throw new IllegalStateException("the peer deletion never returned");
            }
            return peer;
        }

        private static void await(CountDownLatch latch) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
