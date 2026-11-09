package tel.schich.libdatachannel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Detects the libc loaded by this JVM, even when both libcs are installed. */
final class LinuxLibc {
    static final String PROPERTY = "libdatachannel.libc";

    private LinuxLibc() {
    }

    static String detect() {
        String override = System.getProperty(PROPERTY);
        if (override != null) {
            if (!override.equals("glibc") && !override.equals("musl")) {
                throw new UnsatisfiedLinkError(PROPERTY + " must be glibc or musl");
            }
            return override;
        }
        try {
            // Mapped paths are bytes, which ISO-8859-1 decodes without failing
            return fromMappings(Files.readAllLines(Path.of("/proc/self/maps"), StandardCharsets.ISO_8859_1));
        } catch (IOException | SecurityException e) {
            UnsatisfiedLinkError error = unidentified();
            error.initCause(e);
            throw error;
        }
    }

    static String fromMappings(List<String> mappings) {
        boolean glibc = false;
        boolean musl = false;
        for (String mapping : mappings) {
            int pathStart = mapping.indexOf('/');
            if (pathStart < 0) {
                continue;
            }
            String path = mapping.substring(pathStart).replace(" (deleted)", "");
            String name = path.substring(path.lastIndexOf('/') + 1);
            // Debian and Void link the musl loader to a plain libc.so. glibc's is a linker script, never mapped.
            // Android's bionic is a plain libc.so too, under /system or /apex, and neither native fits it.
            boolean bionic = path.startsWith("/system/") || path.startsWith("/apex/") || path.contains("/bionic/");
            if (name.startsWith("ld-musl-") || name.startsWith("libc.musl-") || (name.equals("libc.so") && !bionic)) {
                musl = true;
            } else if (name.equals("libc.so.6") || name.matches("libc-[0-9]+\\.[0-9]+\\.so")) {
                glibc = true;
            }
        }
        // gcompat can map libc.so.6 into a musl JVM, so the musl loader takes precedence.
        if (musl) {
            return "musl";
        }
        if (glibc) {
            return "glibc";
        }
        throw unidentified();
    }

    private static UnsatisfiedLinkError unidentified() {
        return new UnsatisfiedLinkError("Cannot identify the JVM's libc; set -D" + PROPERTY
                + "=glibc or -D" + PROPERTY + "=musl");
    }
}
