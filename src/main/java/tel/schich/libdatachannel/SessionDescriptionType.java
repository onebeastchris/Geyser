package tel.schich.libdatachannel;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;

public enum SessionDescriptionType {
    ANSWER("answer"),
    OFFER("offer"),
    PROVISIONAL_ANSWER("pranswer"),
    ROLLBACK("rollback"),
    ;

    private static final Map<String, SessionDescriptionType> MAP = Util.mappedEnum(SessionDescriptionType.values(), s -> s.type);

    final String type;

    SessionDescriptionType(String type) {
        this.type = type;
    }

    @Nullable
    static SessionDescriptionType of(final String type) {
        return MAP.get(type);
    }
}
