package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DataChannelReliabilityTest {
    @Test
    void changingOrderOrLimitsDoesNotChangeReliability() {
        for (boolean unordered : new boolean[] {false, true}) {
            for (boolean unreliable : new boolean[] {false, true}) {
                var original = new DataChannelReliability(unordered, unreliable, 40, 2);
                var reordered = original.withUnordered(!unordered);
                assertEquals(unreliable, reordered.isUnreliable());
                assertEquals(!unordered, reordered.isUnordered());
                var limited = original.withMaxPacketLifeTime(Duration.ofMillis(90));
                assertEquals(unreliable, limited.isUnreliable());
                assertEquals(unordered, limited.isUnordered());
                assertEquals(Duration.ofMillis(90), limited.maxPacketLifeTime());
                assertEquals(2, limited.maxRetransmits());
                var retried = original.withMaxRetransmits(3);
                assertEquals(unreliable, retried.isUnreliable());
                assertEquals(unordered, retried.isUnordered());
                assertEquals(Duration.ofMillis(40), retried.maxPacketLifeTime());
                assertEquals(3, retried.maxRetransmits());
                assertEquals(Duration.ofMillis(40), original.maxPacketLifeTime());
                assertEquals(2, original.maxRetransmits());
            }
        }
    }
}
