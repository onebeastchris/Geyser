package tel.schich.libdatachannel;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.*;

class CandidatePairTest {
    @Test
    void readsAddressAndTypeFromEachLine() {
        CandidatePair pair = CandidatePair.parse(
                "candidate:2 1 UDP 2114977535 192.168.191.89 19135 typ host",
                "candidate:9 1 UDP 1845501695 203.0.113.9 61000 typ prflx raddr 0.0.0.0 rport 0");
        assertEquals(new InetSocketAddress("192.168.191.89", 19135), pair.local());
        assertEquals("host", pair.localType());
        assertEquals("udp", pair.localTransport());
        assertEquals(new InetSocketAddress("203.0.113.9", 61000), pair.remote());
        assertEquals("prflx", pair.remoteType());
        assertEquals("udp", pair.remoteTransport());
        assertNotNull(pair.remote().getAddress(), "a literal resolves without a lookup");
    }

    @Test
    void readsAnIpv6LiteralAndARelay() {
        CandidatePair pair = CandidatePair.parse(
                "candidate:3 1 UDP 2130706431 2001:db8::1 50001 typ host",
                "candidate:4 1 UDP 16777215 198.51.100.7 3478 typ relay raddr 203.0.113.9 rport 61000");
        assertEquals(new InetSocketAddress("2001:db8::1", 50001), pair.local());
        assertEquals("relay", pair.remoteType());
    }

    @Test
    void leavesAnMdnsNameUnresolvedAndABrokenLineNull() {
        CandidatePair pair = CandidatePair.parse(
                "candidate:1 1 UDP 2130706431 a1b2c3d4-e5f6.local 5000 typ host",
                "candidate:broken");
        assertTrue(pair.local().isUnresolved());
        assertEquals("host", pair.localType());
        assertNull(pair.remote());
        assertNull(pair.remoteType());
        assertNull(pair.remoteTransport());
    }

    @Test
    void readsTransportNamesAndPreservesTheCandidateLines() {
        String local = "candidate:1 1 TcP 123 2001:db8::1 5000 typ relay tcptype passive";
        String remote = "candidate:2 1 FUTURE 456 no-dns.invalid 6000 typ future extension retained";
        CandidatePair pair = CandidatePair.parse(local, remote);
        assertEquals("tcp", pair.localTransport());
        assertEquals("future", pair.remoteTransport());
        assertEquals(local, pair.localCandidate());
        assertEquals(remote, pair.remoteCandidate());
    }
}
