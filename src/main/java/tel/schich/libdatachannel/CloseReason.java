package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

/** Why a peer connection closed, as far as this side can tell. */
public enum CloseReason {
    /** Still open. */
    RTC_CLOSE_REASON_NONE(0),
    /** Closed by this side. */
    RTC_CLOSE_REASON_LOCAL(1),
    /** The remote shut SCTP or DTLS down. */
    RTC_CLOSE_REASON_REMOTE_SHUTDOWN(2),
    /** The remote aborted the SCTP association. */
    RTC_CLOSE_REASON_REMOTE_ABORT(3),
    /** The remote stopped answering SCTP, handshake included. */
    RTC_CLOSE_REASON_SCTP_TIMEOUT(4),
    /** ICE lost connectivity to the remote. */
    RTC_CLOSE_REASON_ICE_FAILED(5),
    /** A transport failed otherwise, or could not start. */
    RTC_CLOSE_REASON_TRANSPORT_FAILED(6),
    ;

    private static final Map<Integer, CloseReason> MAP = Util.mappedEnum(CloseReason.values(), s -> s.reason);

    final int reason;

    CloseReason(int reason) {
        this.reason = reason;
    }

    @Nullable
    public static CloseReason of(final int reason) {
        return MAP.get(reason);
    }
}
