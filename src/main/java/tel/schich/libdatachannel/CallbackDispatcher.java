package tel.schich.libdatachannel;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.Executor;

/** Couples callback execution with the lifetime of borrowed JNI message storage. */
abstract class CallbackDispatcher implements Executor {
    private static final CallbackDispatcher DIRECT = new CallbackDispatcher() {
        @Override
        public void execute(Runnable callback) {
            callback.run();
        }

        @Override
        ByteBuffer prepareBinaryMessage(ByteBuffer message) {
            return message;
        }
    };

    static CallbackDispatcher direct() {
        return DIRECT;
    }

    static CallbackDispatcher on(Executor executor) {
        Objects.requireNonNull(executor, "executor");
        return new CallbackDispatcher() {
            @Override
            public void execute(Runnable callback) {
                executor.execute(callback);
            }

            @Override
            ByteBuffer prepareBinaryMessage(ByteBuffer message) {
                // An executor may defer delivery until after the native callback returns.
                return ByteBuffer.allocateDirect(message.remaining()).put(message.duplicate()).flip();
            }
        };
    }

    abstract ByteBuffer prepareBinaryMessage(ByteBuffer message);
}
