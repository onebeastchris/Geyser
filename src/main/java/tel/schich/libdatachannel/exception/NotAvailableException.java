package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class NotAvailableException extends LibDataChannelException {
    @JNIAccess
    public NotAvailableException(String message) {
        super(ERR_NOT_AVAIL, message);
    }
}
