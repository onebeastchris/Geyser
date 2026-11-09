package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public enum IceState {
    RTC_ICE_NEW(0),
    RTC_ICE_CHECKING(1),
    RTC_ICE_CONNECTED(2),
    RTC_ICE_COMPLETED(3),
    RTC_ICE_FAILED(4),
    RTC_ICE_DISCONNECTED(5),
    RTC_ICE_CLOSED(6),
    ;

    private static final Map<Integer, IceState> MAP = Util.mappedEnum(IceState.values(), s -> s.state);

    final int state;

    IceState(int state) {
        this.state = state;
    }

    @Nullable
    static IceState of(final int state) {
        return MAP.get(state);
    }
}
