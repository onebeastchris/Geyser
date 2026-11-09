package tel.schich.libdatachannel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Exercises the production callback layout with both explicit peer close APIs. */
public final class CallbackCleanupProbe {
    private static final List<WeakReference<Object>> references = new ArrayList<>();

    private static void cycle(boolean await) {
        PeerConnection peer = PeerConnection.createPeer(
                PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true), Runnable::run);
        references.add(new WeakReference<>(peer));
        peer.onStateChange.register((p, state) -> {});
        peer.onDataChannel.register((p, dc) -> {});
        for (String label : new String[] {"ReliableDataChannel", "UnreliableDataChannel"}) {
            DataChannel channel = peer.createDataChannel(label);
            references.add(new WeakReference<>(channel));
            channel.onMessage.register(DataChannelCallback.Message.handleBinary((dc, buffer) -> {}));
            channel.onClosed.register(dc -> {});
            channel.onError.register((dc, error) -> {});
        }
        if (await && !peer.closeAndAwait(Duration.ofSeconds(5))) {
            throw new AssertionError("Native teardown timed out");
        }
        peer.close(); // Also check repeated peer close after closeAndAwait.
    }

    public static void main(String[] args) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LibDataChannel.class);
        Level previousLevel = logger.getLevel();
        boolean previousAdditive = logger.isAdditive();
        List<String> errors = new CopyOnWriteArrayList<>();
        AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
            @Override protected void append(ILoggingEvent event) {
                if (event.getLevel().isGreaterOrEqual(Level.ERROR)) errors.add(event.getFormattedMessage());
            }
        };
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.ERROR);
        logger.setAdditive(false);
        try {
            for (int i = 0; i < 100; i++) cycle(i % 2 == 0);
            // JNI keeps the peer listener in a global reference. Explicit deletion
            // must release it so peers and their data-channel wrappers can be collected.
            for (int i = 0; i < 100 && references.stream().anyMatch(r -> r.get() != null); i++) {
                System.gc();
                Thread.sleep(20);
            }
            long retained = references.stream().filter(r -> r.get() != null).count();
            if (retained != 0) throw new AssertionError("Closed wrappers retained: " + retained);
            if (!errors.isEmpty()) throw new AssertionError("Native cleanup errors: " + errors.size() + "; " + errors.get(0));
            System.out.println("callback-cleanup PASS cycles=100 collectedWrappers=300 nativeErrors=0");
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
            logger.setAdditive(previousAdditive);
            appender.stop();
        }
    }
}
