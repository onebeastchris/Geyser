package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class InvalidException extends LibDataChannelException {
    @JNIAccess
    public InvalidException(String message) {
        super(ERR_INVALID, message);
    }
}
