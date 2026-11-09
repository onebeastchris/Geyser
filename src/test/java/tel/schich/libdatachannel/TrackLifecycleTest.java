package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tel.schich.libdatachannel.exception.InvalidException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(15)
class TrackLifecycleTest {
    private static final PeerConnectionConfiguration CONFIG =
            PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true);
    private static final String SDP = "m=audio 9 UDP/TLS/RTP/SAVPF 111\r\n"
            + "a=mid:audio\r\na=sendonly\r\na=rtpmap:111 opus/48000/2\r\n";

    @Test
    void peerCloseDeletesLocallyAddedSdpTrack() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG);
             Track track = peer.addTrack(SDP)) {
            assertEquals(Track.Direction.RTC_DIRECTION_SENDONLY, track.direction());
            peer.close();
            assertThrows(InvalidException.class, track::direction);
        }
    }

    @Test
    void peerCloseDeletesLocallyAddedConfiguredTrack() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG);
             Track track = peer.addTrack(TrackInit.DEFAULT.withCodec(Track.Codec.RTC_CODEC_OPUS)
                     .withDirection(Track.Direction.RTC_DIRECTION_SENDONLY))) {
            assertEquals(Track.Direction.RTC_DIRECTION_SENDONLY, track.direction());
            peer.close();
            assertThrows(InvalidException.class, track::direction);
        }
    }

    @Test
    void peerCloseDeletesIncomingTrack() throws Exception {
        var received = new CompletableFuture<Track>();
        try (PeerConnection sender = PeerConnection.createPeer(CONFIG);
             PeerConnection receiver = PeerConnection.createPeer(CONFIG)) {
            receiver.onTrack.register((peer, track) -> received.complete(track));
            sender.addTrack(SDP);
            sender.setLocalDescription("offer");
            receiver.setRemoteDescription(sender.localDescription(), SessionDescriptionType.OFFER);
            try (Track track = received.get(5, TimeUnit.SECONDS)) {
                assertEquals(Track.Direction.RTC_DIRECTION_RECVONLY, track.direction());
                receiver.close();
                assertThrows(InvalidException.class, track::direction);
            }
        }
    }

    @Test
    void closeWaitsForNativeTrackArrivalBeforeDeletingItsHandle() throws Exception {
        var received = new CompletableFuture<Track>();
        var releaseCallback = new CountDownLatch(1);
        var closeStarted = new CountDownLatch(1);
        var workers = Executors.newFixedThreadPool(2);
        try (PeerConnection sender = PeerConnection.createPeer(CONFIG);
             PeerConnection receiver = PeerConnection.createPeer(CONFIG)) {
            sender.addTrack(SDP);
            sender.setLocalDescription("offer");
            receiver.setRemoteDescription(sender.localDescription(), SessionDescriptionType.OFFER);
            // Registering flushes the native tracks that arrived while no callback was installed.
            var registration = workers.submit(() -> receiver.onTrack.register((peer, track) -> {
                received.complete(track);
                try {
                    releaseCallback.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }));
            try (Track track = received.get(5, TimeUnit.SECONDS)) {
                var close = workers.submit(() -> {
                    closeStarted.countDown();
                    receiver.close();
                });
                try {
                    assertTrue(closeStarted.await(5, TimeUnit.SECONDS));
                    assertThrows(java.util.concurrent.TimeoutException.class,
                            () -> close.get(100, TimeUnit.MILLISECONDS));
                    assertEquals(Track.Direction.RTC_DIRECTION_RECVONLY, track.direction(),
                            "the arrival callback must finish before its native handle is deleted");
                } finally {
                    releaseCallback.countDown();
                }
                registration.get(5, TimeUnit.SECONDS);
                close.get(5, TimeUnit.SECONDS);
                assertThrows(InvalidException.class, track::direction);
            }
        } finally {
            releaseCallback.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    void asynchronousPeerCloseAlsoDeletesTracks() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG);
             Track track = peer.addTrack(SDP)) {
            assertTrue(peer.closeAndAwait(Duration.ofSeconds(5)));
            assertThrows(InvalidException.class, track::direction);
        }
    }

    @Test
    void explicitTrackCloseIsIdempotentAndPeerCanCloseAfterIt() {
        try (PeerConnection peer = PeerConnection.createPeer(CONFIG)) {
            Track track = peer.addTrack(SDP);
            track.close();
            assertDoesNotThrow(track::close);
        }
    }
}
