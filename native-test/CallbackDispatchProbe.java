package tel.schich.libdatachannel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Closes a peer while libdatachannel is delivering its Closed ICE state from its own thread. That
 * callback is taken out of the peer before it runs, so unregistering it does not wait for it.
 */
public final class CallbackDispatchProbe {
    private static final PeerConnectionConfiguration CONFIG =
            PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    /** The dispatch read the id before the close and looks it up after, so it must find nothing. */
    private static void releasedBeforeLookup() throws InterruptedException {
        LibDataChannelNative.setTestDispatchDelay(200);
        int released;
        try {
            PeerConnection peer = PeerConnection.createPeer(CONFIG, Runnable::run);
            peer.onIceStateChange.register((p, state) -> {});
            peer.closeAsync();
            Thread.sleep(50);
            peer.close();
            Thread.sleep(300);
        } finally {
            released = LibDataChannelNative.setTestDispatchDelay(0);
        }
        check(released >= 1, "a dispatch held across the close found its id released");
    }

    /** The dispatch already holds the listener, so the global reference outlives the close. */
    private static void releasedDuringDispatch() throws InterruptedException {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        PeerConnection peer = PeerConnection.createPeer(CONFIG, Runnable::run);
        WeakReference<PeerConnection> reference = new WeakReference<>(peer);
        peer.onIceStateChange.register((p, state) -> {
            if (state == IceState.RTC_ICE_CLOSED) {
                entered.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        peer.closeAsync();
        check(entered.await(5, TimeUnit.SECONDS), "the Closed ICE state was delivered");
        peer.close();
        release.countDown();
        peer = null;
        // The dispatch drops the last hold on libdatachannel's thread, which releases the peer
        for (int i = 0; i < 100 && reference.get() != null; i++) {
            System.gc();
            Thread.sleep(20);
        }
        check(reference.get() == null, "the peer was collected once the dispatch returned");
    }

    public static void main(String[] args) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LibDataChannel.class);
        List<String> errors = new CopyOnWriteArrayList<>();
        AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
            @Override protected void append(ILoggingEvent event) {
                if (event.getLevel().isGreaterOrEqual(Level.ERROR)) errors.add(event.getFormattedMessage());
            }
        };
        appender.start();
        logger.addAppender(appender);
        try {
            for (int i = 0; i < 5; i++) releasedBeforeLookup();
            for (int i = 0; i < 5; i++) releasedDuringDispatch();
            check(errors.isEmpty(), "native errors: " + errors);
            System.out.println("callback-dispatch PASS releasedBeforeLookup=5 releasedDuringDispatch=5");
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }
}
