package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public enum SignalingState {
    RTC_SIGNALING_STABLE(0),
    RTC_SIGNALING_HAVE_LOCAL_OFFER(1),
    RTC_SIGNALING_HAVE_REMOTE_OFFER(2),
    RTC_SIGNALING_HAVE_LOCAL_PRANSWER(3),
    RTC_SIGNALING_HAVE_REMOTE_PRANSWER(4),
    ;

    private static final Map<Integer, SignalingState> MAP = Util.mappedEnum(SignalingState.values(), s -> s.state);

    final int state;

    SignalingState(int state) {
        this.state = state;
    }

    @Nullable
    public static SignalingState of(final int state) {
        return MAP.get(state);
    }
}
