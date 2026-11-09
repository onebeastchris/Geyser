package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * libdatachannel attaches its own native threads to the JVM whenever they reach a callback. Threads that only live for
 * as long as a peer connection have to be detached again when they terminate, otherwise the JVM keeps a record for
 * every one of them and its thread list grows for the rest of the process.
 */
class NativeThreadLifecycleTest {
    private static final int MEASURED_PEERS = 40;
    /**
     * The threads that stay around are already attached once the count settles, so a healthy run adds none. The slack
     * only absorbs unrelated JVM and logging threads.
     */
    private static final int TOLERATED_GROWTH = 10;
    private static final int SETTLE_SAMPLES = 5;
    private static final int SETTLE_LIMIT = 100;

    @Test
    @Timeout(120)
    void terminatedNativeThreadsDoNotStayAttached() {
        int threadsBefore = churnUntilThreadCountSettles();
        for (int i = 0; i < MEASURED_PEERS; i++) {
            churnPeer();
        }
        int growth = Thread.getAllStackTraces().size() - threadsBefore;

        assertTrue(growth <= TOLERATED_GROWTH,
                MEASURED_PEERS + " peer connections left " + growth + " additional threads registered with the JVM");
    }

    /**
     * libdatachannel's worker pool has a fixed size but attaches its threads lazily, so the thread count keeps climbing
     * for the first few peers even when nothing leaks. Churn peers until it stops moving, or give up and let the
     * assertion report what it sees.
     */
    private static int churnUntilThreadCountSettles() {
        int stableSamples = 0;
        int previousCount = -1;
        for (int i = 0; i < SETTLE_LIMIT && stableSamples < SETTLE_SAMPLES; i++) {
            churnPeer();
            int count = Thread.getAllStackTraces().size();
            stableSamples = count == previousCount ? stableSamples + 1 : 0;
            previousCount = count;
        }
        return previousCount;
    }

    private static void churnPeer() {
        PeerConnectionConfiguration config = PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);
        try (PeerConnection peer = PeerConnection.createPeer(config)) {
            peer.createDataChannel("thread-lifecycle");
            peer.setLocalDescription("offer");
        }
    }
}
