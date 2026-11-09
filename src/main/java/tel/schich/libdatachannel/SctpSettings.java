package tel.schich.libdatachannel;

/**
 * Process-wide SCTP tuning, applied with {@link LibDataChannel#setSctpSettings(SctpSettings)}.
 * <p>
 * Fields left at 0 keep libdatachannel's default. A negative {@code maxBurst} or {@code delayedSackTimeMs}
 * disables that mechanism, and {@code congestionControlModule} keeps the default at -1.
 *
 * @see <a href="https://github.com/paullouisageneau/libdatachannel/blob/master/DOC.md#rtcsetsctpsettings">Documentation</a>
 */
public final class SctpSettings {
  public static final SctpSettings DEFAULT = builder().build();

  final int recvBufferSize, sendBufferSize, maxChunksOnQueue, initialCongestionWindow, maxBurst,
      congestionControlModule, delayedSackTimeMs, minRetransmitTimeoutMs, maxRetransmitTimeoutMs,
      initialRetransmitTimeoutMs, maxRetransmitAttempts, heartbeatIntervalMs;

  private SctpSettings(Builder b) {
    this.recvBufferSize = b.recvBufferSize;
    this.sendBufferSize = b.sendBufferSize;
    this.maxChunksOnQueue = b.maxChunksOnQueue;
    this.initialCongestionWindow = b.initialCongestionWindow;
    this.maxBurst = b.maxBurst;
    this.congestionControlModule = b.congestionControlModule;
    this.delayedSackTimeMs = b.delayedSackTimeMs;
    this.minRetransmitTimeoutMs = b.minRetransmitTimeoutMs;
    this.maxRetransmitTimeoutMs = b.maxRetransmitTimeoutMs;
    this.initialRetransmitTimeoutMs = b.initialRetransmitTimeoutMs;
    this.maxRetransmitAttempts = b.maxRetransmitAttempts;
    this.heartbeatIntervalMs = b.heartbeatIntervalMs;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "SctpSettings{recvBufferSize=" + recvBufferSize + ", sendBufferSize=" + sendBufferSize +
        ", maxChunksOnQueue=" + maxChunksOnQueue + ", initialCongestionWindow=" + initialCongestionWindow +
        ", maxBurst=" + maxBurst + ", congestionControlModule=" + congestionControlModule +
        ", delayedSackTimeMs=" + delayedSackTimeMs + ", minRetransmitTimeoutMs=" + minRetransmitTimeoutMs +
        ", maxRetransmitTimeoutMs=" + maxRetransmitTimeoutMs + ", initialRetransmitTimeoutMs=" +
        initialRetransmitTimeoutMs + ", maxRetransmitAttempts=" + maxRetransmitAttempts +
        ", heartbeatIntervalMs=" + heartbeatIntervalMs + "}";
  }

  public static final class Builder {
    private int recvBufferSize, sendBufferSize, maxChunksOnQueue, initialCongestionWindow, maxBurst,
        delayedSackTimeMs, minRetransmitTimeoutMs, maxRetransmitTimeoutMs, initialRetransmitTimeoutMs,
        maxRetransmitAttempts, heartbeatIntervalMs;
    private int congestionControlModule = -1;

    private Builder() {
    }

    /**
     * Receive buffer in bytes, which bounds the window advertised to the peer.
     */
    public Builder recvBufferSize(int bytes) {
      this.recvBufferSize = bytes;
      return this;
    }

    /**
     * Send buffer in bytes.
     */
    public Builder sendBufferSize(int bytes) {
      this.sendBufferSize = bytes;
      return this;
    }

    public Builder maxChunksOnQueue(int chunks) {
      this.maxChunksOnQueue = chunks;
      return this;
    }

    /**
     * Initial congestion window in MTUs.
     */
    public Builder initialCongestionWindow(int mtus) {
      this.initialCongestionWindow = mtus;
      return this;
    }

    /**
     * Most packets sent at once, in MTUs.
     */
    public Builder maxBurst(int mtus) {
      this.maxBurst = mtus;
      return this;
    }

    /**
     * 0: RFC 2581, 1: HSTCP, 2: H-TCP, 3: RTCC.
     */
    public Builder congestionControlModule(int module) {
      if (module < -1 || module > 3) {
        throw new IllegalArgumentException("Unknown congestion control module " + module);
      }
      this.congestionControlModule = module;
      return this;
    }

    public Builder delayedSackTimeMs(int millis) {
      this.delayedSackTimeMs = millis;
      return this;
    }

    public Builder minRetransmitTimeoutMs(int millis) {
      this.minRetransmitTimeoutMs = millis;
      return this;
    }

    public Builder maxRetransmitTimeoutMs(int millis) {
      this.maxRetransmitTimeoutMs = millis;
      return this;
    }

    public Builder initialRetransmitTimeoutMs(int millis) {
      this.initialRetransmitTimeoutMs = millis;
      return this;
    }

    /**
     * Consecutive retransmissions before the association is given up.
     */
    public Builder maxRetransmitAttempts(int attempts) {
      this.maxRetransmitAttempts = attempts;
      return this;
    }

    public Builder heartbeatIntervalMs(int millis) {
      this.heartbeatIntervalMs = millis;
      return this;
    }

    public SctpSettings build() {
      return new SctpSettings(this);
    }
  }
}
