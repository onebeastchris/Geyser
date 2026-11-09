package tel.schich.libdatachannel;

import java.nio.ByteBuffer;

/**
 * Callback interfaces for {@link DataChannel}
 */
public interface DataChannelCallback {
    /**
     * Called when the channel was previously connecting and is now open.
     */
    @FunctionalInterface
    interface Open {
        void onOpen(DataChannel channel);
    }

    /**
     * Called when the channel was previously open and is now closed.
     */
    @FunctionalInterface
    interface Closed {
        void onClosed(DataChannel channel);
    }

    /**
     * Called when the channel experiences an error, either while connecting or open.
     */
    @FunctionalInterface
    interface Error {
        void onError(DataChannel channel, final String error);
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    interface Message extends TextMessage, BinaryMessage {
        static Message handleText(TextMessage handler) {
            return new DataChannelCallback.Message() {
                @Override
                public void onText(DataChannel channel, String text) {
                    handler.onText(channel, text);
                }

                @Override
                public void onBinary(DataChannel channel, ByteBuffer buffer) {
                }
            };
        }

        static Message handleBinary(BinaryMessage handler) {
            return new DataChannelCallback.Message() {
                @Override
                public void onText(DataChannel channel, String text) {

                }

                @Override
                public void onBinary(DataChannel channel, ByteBuffer buffer) {
                    handler.onBinary(channel, buffer);
                }
            };
        }
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    @FunctionalInterface
    interface TextMessage {
        void onText(DataChannel channel, String text);
    }

    /**
     * Called when the channel receives a message.
     * <p>
     * While set, messages can't be received with {@link DataChannel#receiveMessage()}.
     * </p>
     */
    @FunctionalInterface
    interface BinaryMessage {
        /**
         * The buffer is only valid during this callback; copy bytes that must outlive it.
         * The default synchronous executor receives borrowed native storage. Custom executors
         * receive an owned copy made before the native callback returns.
         */
        void onBinary(DataChannel channel, ByteBuffer buffer);
    }

    /**
     * Called when the buffered amount was strictly higher than the threshold (see {@link DataChannel#bufferedAmountLowThreshold}) and is now lower or
     * equal than the threshold.
     */
    @FunctionalInterface
    interface BufferedAmountLow {
        void onBufferedAmountLow(DataChannel channel);
    }

    /**
     * Called when messages are now available to be received with {@link DataChannel#receiveMessage()}.
     */
    @FunctionalInterface
    interface Available {
        void onAvailable(DataChannel channel);
    }
}
