package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class NativeOperationException extends LibDataChannelException {
    @JNIAccess
    public NativeOperationException(String message, int errorNumber, String strerror) {
        super(errorNumber, message + " (" + strerror + ")");
    }
}
