package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class BinaryCallbackOwnershipTest {
    private static final PeerConnectionConfiguration CONFIG =
            PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);

    @Test
    void deferredCallbackOwnsItsBytesBeforeNativeStorageCanBeReused() {
        Queue<Runnable> callbacks = new ArrayDeque<>();
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG, callbacks::add)) {
            DataChannel channel = peer.createDataChannel("deferred");
            AtomicReference<ByteBuffer> received = new AtomicReference<>();
            channel.onMessage.register(DataChannelCallback.Message.handleBinary((ignored, data) -> received.set(data)));
            ByteBuffer borrowed = ByteBuffer.allocateDirect(4).putInt(0, 1234);

            peer.listener.onChannelBinaryMessage(channel.channelHandle, borrowed);
            assertNull(received.get());
            borrowed.putInt(0, 5678); // native storage can be reused once the JNI callback returns
            while (!callbacks.isEmpty()) callbacks.remove().run();

            assertEquals(1234, received.get().getInt(0));
            assertTrue(received.get().isDirect());
        }
    }

    @Test
    void defaultSynchronousCallbackKeepsBorrowedBufferWithoutAnExtraCopy() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            DataChannel channel = peer.createDataChannel("direct");
            ByteBuffer borrowed = ByteBuffer.allocateDirect(4).putInt(0, 1234);
            AtomicReference<ByteBuffer> received = new AtomicReference<>();
            channel.onMessage.register(DataChannelCallback.Message.handleBinary((ignored, data) -> {
                assertSame(borrowed, data);
                assertEquals(1234, data.getInt(0));
                received.set(data);
            }));

            peer.listener.onChannelBinaryMessage(channel.channelHandle, borrowed);
            assertSame(borrowed, received.get());
        }
    }

    @Test
    void closingBeforeDeferredDeliveryDropsTheCallback() {
        Queue<Runnable> callbacks = new ArrayDeque<>();
        AtomicReference<ByteBuffer> received = new AtomicReference<>();
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG, callbacks::add)) {
            DataChannel channel = peer.createDataChannel("closed");
            channel.onMessage.register(DataChannelCallback.Message.handleBinary((ignored, data) -> received.set(data)));
            peer.listener.onChannelBinaryMessage(channel.channelHandle, ByteBuffer.allocateDirect(4));
            channel.close();
            while (!callbacks.isEmpty()) callbacks.remove().run();
            assertNull(received.get());
        }
    }
}
