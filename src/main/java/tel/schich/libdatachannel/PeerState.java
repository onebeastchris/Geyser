package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public enum PeerState {
    RTC_CONNECTING(1),
    RTC_CONNECTED(2),
    RTC_DISCONNECTED(3),
    RTC_FAILED(4),
    RTC_CLOSED(5),
    ;

    private static final Map<Integer, PeerState> MAP = Util.mappedEnum(PeerState.values(), s -> s.state);

    final int state;

    PeerState(int state) {
        this.state = state;
    }

    @Nullable
    public static PeerState of(final int state) {
        return MAP.get(state);
    }
}
