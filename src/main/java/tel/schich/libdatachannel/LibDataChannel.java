package tel.schich.libdatachannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tel.schich.jniaccess.JNIAccess;

import java.lang.ref.Cleaner;
import java.util.Objects;

public abstract class LibDataChannel {
    static final Cleaner CLEANER = Cleaner.create();
    private static final Logger LOGGER = LoggerFactory.getLogger(LibDataChannel.class);
    private static volatile boolean initialized = false;
    // Read by JNI_OnLoad before rtcPreload can emit transport logs.
    private static int nativeLogLevel = LogLevel.WARNING.value;
    private static SctpSettings sctpSettings;

    /** Native filtering happens before a message crosses into Java. */
    public enum LogLevel {
        NONE(0), FATAL(1), ERROR(2), WARNING(3), INFO(4), DEBUG(5), VERBOSE(6);

        final int value;
        LogLevel(int value) { this.value = value; }
    }

    public static final String LIB_NAME = "datachannel-java";

    private LibDataChannel() {}

    /** Sets the process-wide native log threshold, before or after initialization. */
    public static synchronized void setLogLevel(LogLevel level) {
        nativeLogLevel = Objects.requireNonNull(level, "level").value;
        if (initialized) setLogLevelNative(nativeLogLevel);
    }

    public static synchronized LogLevel logLevel() {
        return LogLevel.values()[nativeLogLevel];
    }

    @JNIAccess
    private static int initialNativeLogLevel() { return nativeLogLevel; }

    private static native void setLogLevelNative(int level);

    /**
     * Sets SCTP tuning for every association created afterwards, before or after initialization, so it has to come
     * before the first {@link PeerConnection}. The settings are process-wide and each call replaces the whole set,
     * so every user of the library in the process shares the last one.
     */
    public static synchronized void setSctpSettings(SctpSettings settings) {
        sctpSettings = Objects.requireNonNull(settings, "settings");
        if (initialized) applySctpSettings(settings);
    }

    private static void applySctpSettings(SctpSettings s) {
        setSctpSettingsNative(s.recvBufferSize, s.sendBufferSize, s.maxChunksOnQueue, s.initialCongestionWindow,
            s.maxBurst, s.congestionControlModule, s.delayedSackTimeMs, s.minRetransmitTimeoutMs,
            s.maxRetransmitTimeoutMs, s.initialRetransmitTimeoutMs, s.maxRetransmitAttempts, s.heartbeatIntervalMs);
    }

    private static native void setSctpSettingsNative(int recvBufferSize, int sendBufferSize, int maxChunksOnQueue,
                                                     int initialCongestionWindow, int maxBurst,
                                                     int congestionControlModule, int delayedSackTimeMs,
                                                     int minRetransmitTimeoutMs, int maxRetransmitTimeoutMs,
                                                     int initialRetransmitTimeoutMs, int maxRetransmitAttempts,
                                                     int heartbeatIntervalMs);

    /**
     * Initializes the library by loading the native library.
     */
    public synchronized static void initialize() {
        if (initialized) {
            return;
        }

        Platform.loadNativeLibrary(LIB_NAME, LibDataChannel.class);
        if (sctpSettings != null) applySctpSettings(sctpSettings);

        initialized = true;
    }

    /**
     * @param level the log level
     * @param message the message to log
     * @see <a href="https://github.com/paullouisageneau/libdatachannel/blob/master/DOC.md#rtcinitlogger">Documentation</a>
     */
    @JNIAccess
    static void log(int level, String message) {
        switch (level) {
            case 1:
            case 2:
                LOGGER.error(message);
                return;
            case 3:
                LOGGER.warn(message);
                return;
            case 4:
                LOGGER.info(message);
                return;
            case 5:
                LOGGER.debug(message);
                return;
            case 6:
                LOGGER.trace(message);
        }
    }

    @JNIAccess
    private static void freeOnGarbageCollection(Object owner, long nativeAddress) {
        CLEANER.register(owner, () -> freeMemory(nativeAddress));
    }

    private static native void freeMemory(long pointer);
}
