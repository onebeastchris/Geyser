package tel.schich.libdatachannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

class Platform {
    private static final Logger LOGGER = LoggerFactory.getLogger(Platform.class);

    private static final String LIB_PREFIX = "/native";
    private static final String PATH_PROP_PREFIX = "libdatachannel.native.";
    private static final String PATH_PROP_FS_PATH = ".path";
    private static final String PATH_PROP_CLASS_PATH = ".classpath";

    /**
     * Checks if the currently running OS is Linux
     *
     * @return true if running on Linux
     */
    public static boolean isLinux() {
        return System.getProperty("os.name").equalsIgnoreCase("Linux");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isAndroid() {
        try {
            return System.getProperty("java.specification.vendor").contains("Android") ||
                    System.getProperty("java.vendor").contains("Android") ||
                    System.getProperty("java.vm.vendor").contains("Android");
        } catch (SecurityException e) {
            return System.getProperty("java.vm.name").toLowerCase().contains("dalvik") ||
                    System.getProperty("java.vm.name").toLowerCase().contains("art");
        }
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static OS getOS() {
        if (isAndroid()) {
            return OS.ANDROID;
        } else if (isLinux()) {
            return OS.LINUX;
        } else if (isMacOS()) {
            return OS.MACOS;
        } else if (isWindows()) {
            return OS.WINDOWS;
        } else {
            return OS.UNKNOWN;
        }
    }


    public static void loadNativeLibrary(String name, Class<?> base) {
        try {
            System.loadLibrary(name);
            LOGGER.trace("Loaded native library {} from library path", name);
        } catch (LinkageError e) {
            loadExplicitLibrary(name, base);
        }
    }

    public static String classPathPropertyNameForLibrary(String name) {
        return PATH_PROP_PREFIX + name.toLowerCase() + PATH_PROP_CLASS_PATH;
    }

    private static String archPrefixForOs() {
        switch (getOS()) {
            case LINUX: {
                return "linux-" + LinuxLibc.detect() + "-";
            }
            case WINDOWS: {
                return "windows-";
            }
            case ANDROID: {
                return "android-";
            }
            case MACOS: {
                return "macos-";
            }
            default: {
                return "";
            }
        }
    }

    private static String detectCpuArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        return detectCpuArch(arch, getOS());
    }

    static String detectCpuArch(String arch, OS os) {
        if (arch.equals("aarch64") || arch.equals("arm64")) {
            if (os == OS.MACOS) {
                return "arm64";
            }
            return "aarch64";
        } else if (arch.contains("arm")) {
            return "armv7";
        } else if (arch.contains("86") || arch.contains("amd")) {
            if (arch.contains("64")) {
                return "x86_64";
            }
            return "x86_32";
        } else if (arch.contains("riscv")) {
            if (arch.contains("64")) {
                return "riscv64";
            }
            return "riscv32";
        }
        return arch;
    }

    public static String detectArch() {
        return archPrefixForOs() + detectCpuArch();
    }

    public static String libraryFilename(String name) {
        final String libName = "lib" + name;
        if (getOS() == OS.WINDOWS) {
            return libName + ".dll";
        } else if (getOS() == OS.MACOS) {
            return libName + ".dylib";
        }
        return libName + ".so";
    }

    private static void loadExplicitLibrary(String name, Class<?> base) {
        String explicitLibraryPath = System.getProperty(PATH_PROP_PREFIX + name.toLowerCase() + PATH_PROP_FS_PATH);
        if (explicitLibraryPath != null) {
            LOGGER.trace("Loading native library {} from {}", name, explicitLibraryPath);
            System.load(explicitLibraryPath);
            return;
        }

        String explicitClassPath = System.getProperty(classPathPropertyNameForLibrary(name));
        final String sourceLibPath;
        if (explicitClassPath != null) {
            sourceLibPath = explicitClassPath;
        } else {
            sourceLibPath = classPathLocation(name, base, detectArch());
        }
        LOGGER.trace("Loading native library {} from {}", name, sourceLibPath);
        try {
            final Path tempDirectory = Files.createTempDirectory(name + "-");
            final Path libPath = tempDirectory.resolve(libraryFilename(name));
            loadFromClassPath(name, base, sourceLibPath, libPath);
        } catch (IOException e) {
            throw new LinkageError("Unable to load native library " + name + "!", e);
        }
    }

    /**
     * Where on the classpath the native is taken from: the explicit property when set, otherwise
     * the per architecture layout of the arch-detect bundle when it holds this platform, otherwise
     * the single native layout of a classifier artifact. So neither layout needs any setup code.
     */
    static String classPathLocation(String name, Class<?> base, String arch) {
        String explicit = System.getProperty(classPathPropertyNameForLibrary(name));
        if (explicit != null) {
            return explicit;
        }
        final String libName = libraryFilename(name);
        final String bundled = "/" + arch + "/native/" + libName;
        if (base.getResource(bundled) != null) {
            return bundled;
        }
        return LIB_PREFIX + "/" + libName;
    }

    private static void loadFromClassPath(String name, Class<?> base, String classPath, Path fsPath) throws IOException {
        try (InputStream libStream = base.getResourceAsStream(classPath)) {
            if (libStream == null) {
                throw new LinkageError("Failed to load the native library " + name + ": " + classPath + " not found.");
            }

            Files.copy(libStream, fsPath, StandardCopyOption.REPLACE_EXISTING);

            System.load(fsPath.toString());
            fsPath.toFile().deleteOnExit();
        }
    }

    public enum OS {
        LINUX,
        WINDOWS,
        ANDROID,
        MACOS,
        UNKNOWN,
    }
}