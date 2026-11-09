package tel.schich.libdatachannel.exception;

public class LibDataChannelException extends RuntimeException {
    public static final int ERR_SUCCESS = 0;
    public static final int ERR_INVALID = -1;
    public static final int ERR_FAILURE = -2;
    public static final int ERR_NOT_AVAIL = -3;
    public static final int ERR_TOO_SMALL = -4;

    private final int errorCode;

    public LibDataChannelException(final int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int errorCode() {
        return errorCode;
    }
}
