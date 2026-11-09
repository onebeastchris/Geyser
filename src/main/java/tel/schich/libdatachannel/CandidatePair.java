package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * The candidate pair ICE selected, as the candidate lines the native library reports them.
 * <p>
 * The remote side is where traffic actually arrives from, which behind a NAT is usually a peer
 * reflexive candidate the remote description never carried. For a pair that is not relayed, libjuice
 * sends from one socket and does not track which local candidate it used, so the local side is the
 * first candidate it gathered.
 * </p>
 */
public class CandidatePair {
    private final String local;
    private final String remote;

    CandidatePair(String local, String remote) {
        this.local = local;
        this.remote = remote;
    }

    /**
     * @return the local candidate line, as {@code candidate:...}
     */
    public String localCandidate() {
        return local;
    }

    /**
     * @return the remote candidate line, as {@code candidate:...}
     */
    public String remoteCandidate() {
        return remote;
    }

    /**
     * @return the local candidate's address, or null if the line cannot be read; only an mDNS name is left unresolved
     */
    public InetSocketAddress local() {
        return address(local);
    }

    /**
     * @return the remote candidate's address, or null if the line cannot be read; only an mDNS name is left unresolved
     */
    public InetSocketAddress remote() {
        return address(remote);
    }

    /**
     * @return the local candidate's type, {@code host}, {@code srflx}, {@code prflx} or {@code relay}, or null if the
     * line cannot be read
     */
    public String localType() {
        return type(local);
    }

    /**
     * @return the remote candidate's type, {@code host}, {@code srflx}, {@code prflx} or {@code relay}, or null if the
     * line cannot be read
     */
    public String remoteType() {
        return type(remote);
    }

    /**
     * @return the local candidate's transport in lower case, usually {@code udp} or {@code tcp}, or null if
     * the line cannot be read
     */
    public String localTransport() {
        return transport(local);
    }

    /**
     * @return the remote candidate's transport in lower case, usually {@code udp} or {@code tcp}, or null if
     * the line cannot be read
     */
    public String remoteTransport() {
        return transport(remote);
    }

    @JNIAccess
    static CandidatePair parse(String local, String remote) {
        return new CandidatePair(local, remote);
    }

    // candidate:<foundation> <component> <transport> <priority> <address> <port> typ <type> ...
    static InetSocketAddress address(String candidate) {
        String[] fields = candidate.split(" ");
        if (fields.length < 6) {
            return null;
        }
        int port;
        try {
            port = Integer.parseInt(fields[5]);
        } catch (NumberFormatException e) {
            return null;
        }
        String host = fields[4];
        // A literal resolves without a lookup, anything else would be an mDNS name and must not trigger one
        boolean literal = host.indexOf(':') >= 0 || host.matches("[0-9.]+");
        if (!literal) {
            return InetSocketAddress.createUnresolved(host, port);
        }
        try {
            return new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            return InetSocketAddress.createUnresolved(host, port);
        }
    }

    static String type(String candidate) {
        String[] fields = candidate.split(" ");
        return fields.length >= 8 && "typ".equals(fields[6]) ? fields[7].toLowerCase(Locale.ROOT) : null;
    }

    static String transport(String candidate) {
        String[] fields = candidate.split(" ");
        return fields.length >= 8 && "typ".equals(fields[6]) ? fields[2].toLowerCase(Locale.ROOT) : null;
    }
}
