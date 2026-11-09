package tel.schich.libdatachannel;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** Runs against packaged release natives in a JVM for the target platform. */
public final class NativeLibrarySmoke {
    public static void main(String[] args) throws Exception {
        String classifier = Platform.detectArch();
        if (!classifier.equals(args[0])) {
            throw new AssertionError("Expected " + args[0] + ", detected " + classifier);
        }
        if (args.length > 1 && args[1].equals("eager")) {
            Class.forName("tel.schich.libdatachannel.LibDataChannelArchDetect")
                    .getMethod("initialize").invoke(null);
        }
        ArrayBlockingQueue<String> descriptions = new ArrayBlockingQueue<>(1);
        try (PeerConnection peer = PeerConnection.createPeer(
                PeerConnectionConfiguration.DEFAULT.withDisableAutoNegotiation(true))) {
            peer.onLocalDescription.register((connection, sdp, type) -> descriptions.offer(sdp));
            peer.createDataChannel("libc-smoke");
            peer.setLocalDescription("offer");
            String description = descriptions.poll(10, TimeUnit.SECONDS);
            if (description == null || !description.contains("a=fingerprint:sha-256")) {
                throw new AssertionError("Native offer/certificate callback did not complete");
            }
            if (!peer.closeAndAwait(Duration.ofSeconds(10))) {
                throw new AssertionError("Native peer cleanup did not complete");
            }
        }
        System.out.println("Native loading, certificate, callback and cleanup passed: " + classifier);
    }
}
