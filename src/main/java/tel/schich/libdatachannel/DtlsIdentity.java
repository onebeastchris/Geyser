package tel.schich.libdatachannel;

import java.nio.file.Path;
import java.util.Objects;
import org.eclipse.jdt.annotation.Nullable;

/** A paired PEM certificate and private key supplied by the application. */
public final class DtlsIdentity {
    private final Path certificate, privateKey;
    private final @Nullable String password;

    public DtlsIdentity(Path certificate, Path privateKey) {
        this(certificate, privateKey, null);
    }

    public DtlsIdentity(Path certificate, Path privateKey, @Nullable String password) {
        this.certificate = Objects.requireNonNull(certificate, "certificate");
        this.privateKey = Objects.requireNonNull(privateKey, "privateKey");
        this.password = password;
    }

    public Path certificate() { return certificate; }
    public Path privateKey() { return privateKey; }
    public @Nullable String password() { return password; }
}
