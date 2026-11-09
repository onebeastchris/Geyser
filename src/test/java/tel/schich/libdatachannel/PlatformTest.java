package tel.schich.libdatachannel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Both native layouts have to work without setup code: the arch-detect bundle keeps one native
 * per architecture, a classifier artifact keeps one at the root.
 */
class PlatformTest {
    private static final String PROPERTY = Platform.classPathPropertyNameForLibrary("probe");

    @AfterEach
    void clearProperty() {
        System.clearProperty(PROPERTY);
    }

    @Test
    void prefersTheBundledNativeForThisArchitecture() {
        assertEquals("/test-arch/native/" + Platform.libraryFilename("probe"),
                Platform.classPathLocation("probe", PlatformTest.class, "test-arch"));
    }

    @Test
    void fallsBackToTheClassifierLayoutWhenTheBundleLacksThisArchitecture() {
        assertEquals("/native/" + Platform.libraryFilename("probe"),
                Platform.classPathLocation("probe", PlatformTest.class, "other-arch"));
    }

    @Test
    void anExplicitPropertyStillWins() {
        System.setProperty(PROPERTY, "/elsewhere/libprobe.so");
        assertEquals("/elsewhere/libprobe.so", Platform.classPathLocation("probe", PlatformTest.class, "test-arch"));
    }
}
