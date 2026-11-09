package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class TooSmallException extends LibDataChannelException {
    @JNIAccess
    public TooSmallException(String message) {
        super(ERR_TOO_SMALL, message);
    }
}
