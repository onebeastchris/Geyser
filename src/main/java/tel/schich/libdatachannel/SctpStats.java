package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.time.Duration;

/** What a peer's SCTP association reported about itself when it was read. */
public final class SctpStats {
    private final Duration rtt, rto;
    private final long congestionWindow, peerReceiveWindow, unackedChunks, pendingChunks, dataTimeouts;

    private SctpStats(Duration rtt, Duration rto, long congestionWindow, long peerReceiveWindow,
                      long unackedChunks, long pendingChunks, long dataTimeouts) {
        this.rtt = rtt;
        this.rto = rto;
        this.congestionWindow = congestionWindow;
        this.peerReceiveWindow = peerReceiveWindow;
        this.unackedChunks = unackedChunks;
        this.pendingChunks = pendingChunks;
        this.dataTimeouts = dataTimeouts;
    }

    @JNIAccess
    static SctpStats create(int rttMillis, int rtoMillis, long congestionWindow, long peerReceiveWindow,
                            long unackedChunks, long pendingChunks, long dataTimeouts) {
        return new SctpStats(Duration.ofMillis(rttMillis), Duration.ofMillis(rtoMillis), congestionWindow,
            peerReceiveWindow, unackedChunks, pendingChunks, dataTimeouts);
    }

    /** Smoothed round trip time in whole milliseconds, zero until sampled or while under one. */
    public Duration rtt() { return rtt; }
    /** Retransmission timeout, which grows as acknowledgements go missing. */
    public Duration rto() { return rto; }
    /** Congestion window in bytes. */
    public long congestionWindow() { return congestionWindow; }
    /** Receive window the peer last advertised, in bytes. */
    public long peerReceiveWindow() { return peerReceiveWindow; }
    /** DATA chunks sent and not acknowledged yet. */
    public long unackedChunks() { return unackedChunks; }
    /** Received chunks waiting for reassembly or ordering. */
    public long pendingChunks() { return pendingChunks; }
    /** Retransmission timer expiries since the association started, each a stall while data is resent. */
    public long dataTimeouts() { return dataTimeouts; }

    @Override
    public String toString() {
        return "SctpStats{rtt=" + rtt.toMillis() + "ms, rto=" + rto.toMillis() + "ms, congestionWindow=" +
            congestionWindow + ", peerReceiveWindow=" + peerReceiveWindow + ", unackedChunks=" + unackedChunks +
            ", pendingChunks=" + pendingChunks + ", dataTimeouts=" + dataTimeouts + "}";
    }
}
