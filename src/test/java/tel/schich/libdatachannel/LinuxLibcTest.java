package tel.schich.libdatachannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinuxLibcTest {
    @AfterEach
    void clearOverride() {
        System.clearProperty(LinuxLibc.PROPERTY);
    }

    @Test
    void identifiesGlibcWithBothLibraryNamingSchemes() {
        assertEquals("glibc", LinuxLibc.fromMappings(List.of("0000 r-xp /usr/lib64/libc.so.6")));
        assertEquals("glibc", LinuxLibc.fromMappings(List.of("0000 r-xp /lib/libc-2.31.so (deleted)")));
    }

    @Test
    void identifiesMuslEvenWhenGcompatIsLoaded() {
        assertEquals("musl", LinuxLibc.fromMappings(List.of(
                "0000 r-xp /lib/libc.so.6", "0000 r-xp /lib/ld-musl-x86_64.so.1")));
        assertEquals("musl", LinuxLibc.fromMappings(List.of("0000 r-xp /lib/libc.musl-aarch64.so.1")));
    }

    @Test
    void identifiesMuslLinkedAsAPlainLibcSo() {
        assertEquals("musl", LinuxLibc.fromMappings(List.of("0000 r-xp /usr/lib/x86_64-linux-musl/libc.so")));
        assertEquals("musl", LinuxLibc.fromMappings(List.of("0000 r-xp /usr/lib/libc.so")));
        assertEquals("musl", LinuxLibc.fromMappings(List.of(
                "0000 r-xp /usr/lib/libc.so.6", "0000 r-xp /usr/lib/libc.so")));
    }

    @Test
    void bionicIsNotTakenForMusl() {
        assertThrows(UnsatisfiedLinkError.class, () -> LinuxLibc.fromMappings(
                List.of("0000 r-xp /apex/com.android.runtime/lib64/bionic/libc.so")));
        assertThrows(UnsatisfiedLinkError.class,
                () -> LinuxLibc.fromMappings(List.of("0000 r-xp /system/lib64/libc.so")));
    }

    @Test
    void unknownMappingsRequireAnExplicitOverride() {
        assertThrows(UnsatisfiedLinkError.class,
                () -> LinuxLibc.fromMappings(List.of("0000 r-xp /home/musl/libjvm.so")));
        System.setProperty(LinuxLibc.PROPERTY, "musl");
        assertEquals("musl", LinuxLibc.detect());
        System.setProperty(LinuxLibc.PROPERTY, "glibc");
        assertEquals("glibc", LinuxLibc.detect());
        System.setProperty(LinuxLibc.PROPERTY, "unknown");
        assertThrows(UnsatisfiedLinkError.class, LinuxLibc::detect);
    }

    @Test
    void arm64AliasesDoNotSelectArmv7() {
        assertEquals("aarch64", Platform.detectCpuArch("arm64", Platform.OS.LINUX));
        assertEquals("aarch64", Platform.detectCpuArch("aarch64", Platform.OS.LINUX));
        assertEquals("arm64", Platform.detectCpuArch("aarch64", Platform.OS.MACOS));
        assertEquals("armv7", Platform.detectCpuArch("armv7l", Platform.OS.LINUX));
        assertEquals("x86_64", Platform.detectCpuArch("amd64", Platform.OS.LINUX));
    }
}
