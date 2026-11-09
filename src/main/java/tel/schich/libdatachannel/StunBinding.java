package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.time.Duration;
import java.util.Optional;

/** Owned observation from one resolved STUN server; a mapping is not a reachability verdict. */
public final class StunBinding {
    public enum State { PENDING, SUCCEEDED, FAILED }

    private final String serverAddress, mappedAddress;
    private final int serverPort, mappedPort;
    private final State state;
    private final long successfulResponses, failedTransactions, mappingRevision;
    private final long successAgeMillis, sampledAtNanos;

    private StunBinding(String serverAddress, int serverPort, String mappedAddress, int mappedPort,
                        int state, long successfulResponses, long failedTransactions, long mappingRevision,
                        long successAgeMillis, long sampledAtNanos) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.mappedAddress = mappedAddress;
        this.mappedPort = mappedPort;
        this.state = State.values()[state];
        this.successfulResponses = successfulResponses;
        this.failedTransactions = failedTransactions;
        this.mappingRevision = mappingRevision;
        this.successAgeMillis = successAgeMillis;
        this.sampledAtNanos = sampledAtNanos;
    }

    @JNIAccess
    static StunBinding create(String serverAddress, int serverPort, String mappedAddress, int mappedPort,
                              int state, long successfulResponses, long failedTransactions, long mappingRevision,
                              long successAgeMillis, long sampledAtNanos) {
        return new StunBinding(serverAddress, serverPort, mappedAddress, mappedPort, state,
            successfulResponses, failedTransactions, mappingRevision, successAgeMillis, sampledAtNanos);
    }

    public String serverAddress() { return serverAddress; }
    public int serverPort() { return serverPort; }
    /** Empty until the first accepted response. */
    public String mappedAddress() { return mappedAddress; }
    /** Zero until the first accepted response. */
    public int mappedPort() { return mappedPort; }
    public State state() { return state; }
    public long successfulResponses() { return successfulResponses; }
    public long failedTransactions() { return failedTransactions; }
    public long mappingRevision() { return mappingRevision; }

    /**
     * Empty before any successful observation. Age continues increasing on
     * retained snapshots using the JVM monotonic clock, including JNI read time.
     * Reading this value never renews the native observation.
     */
    public Optional<Duration> lastSuccessAge() {
        if (successAgeMillis < 0) return Optional.empty();
        long elapsedMillis = Math.max(0, (System.nanoTime() - sampledAtNanos) / 1_000_000);
        long age = successAgeMillis > Long.MAX_VALUE - elapsedMillis
            ? Long.MAX_VALUE : successAgeMillis + elapsedMillis;
        return Optional.of(Duration.ofMillis(age));
    }
}
