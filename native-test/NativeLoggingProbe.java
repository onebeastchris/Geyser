package tel.schich.libdatachannel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Native log filtering must happen before JNI even when Java accepts every level. */
public final class NativeLoggingProbe {
    static void cycle() {
        try (PeerConnection peer = PeerConnection.createPeer(PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true))) {
            peer.createDataChannel("log-threshold");
            peer.setLocalDescription("offer", "loggerFixture", "fixturePassword0000000000");
            if (!peer.closeAndAwait(Duration.ofSeconds(5))) throw new AssertionError("logging fixture teardown");
        }
    }
    public static void main(String[] args) throws Exception {
        Logger logger = (Logger) LoggerFactory.getLogger(LibDataChannel.class);
        Level previous = logger.getLevel();
        boolean additive = logger.isAdditive();
        List<ILoggingEvent> events = new CopyOnWriteArrayList<>();
        AppenderBase<ILoggingEvent> appender = new AppenderBase<>() {
            @Override protected void append(ILoggingEvent event) { events.add(event); }
        };
        appender.start(); logger.addAppender(appender); logger.setLevel(Level.TRACE); logger.setAdditive(false);
        try {
            if (LibDataChannel.logLevel() != LibDataChannel.LogLevel.WARNING) throw new AssertionError("default native threshold");
            LibDataChannel.setLogLevel(LibDataChannel.LogLevel.NONE); // Before loading/preloading native code.
            cycle();
            if (!events.isEmpty()) throw new AssertionError("native logs crossed JNI with logging disabled");
            LibDataChannel.setLogLevel(LibDataChannel.LogLevel.DEBUG);
            cycle();
            if (events.stream().noneMatch(e -> e.getLevel() == Level.DEBUG)) throw new AssertionError("native threshold did not update after initialization");
            LibDataChannel.setLogLevel(LibDataChannel.LogLevel.WARNING);
            events.clear(); cycle();
            if (events.stream().anyMatch(e -> !e.getLevel().isGreaterOrEqual(Level.WARN))) throw new AssertionError("filtered native transport log crossed JNI");
            System.out.println("native-logging PASS default=WARNING beforeLoad=NONE afterLoad=DEBUG,WARNING nativeFilter=true");
        } finally {
            LibDataChannel.setLogLevel(LibDataChannel.LogLevel.WARNING);
            logger.detachAppender(appender); logger.setLevel(previous); logger.setAdditive(additive); appender.stop();
        }
    }
}
