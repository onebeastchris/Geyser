# libdatachannel-java
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

Java wrappers for [libdatachannel](https://github.com/paullouisageneau/libdatachannel), a WebRTC Data Channels standalone implementation in C++.

## Usage

#### Gradle (build.gradle.kts)
```kotlin
implementation("dev.opencollab:libdatachannel-java:0.24.1.1")
```

#### Maven (pom.xml)
```xml
<dependency>
    <groupId>dev.opencollab</groupId>
    <artifactId>libdatachannel-java</artifactId>
    <version>0.24.1.1</version>
</dependency>
```

Additionally, pull the architecture-specific native components using their the architecture-specific classifier.

Alternatively, use the `libdatachannel-java-arch-detect` module, which includes common architectures and has
code to detect which one to apply, including the libc loaded by a Linux JVM.

Linux classifiers are `linux-glibc-x86_64`, `linux-glibc-aarch64`,
`linux-musl-x86_64`, and `linux-musl-aarch64`. Alpine uses the musl variants.
These replace the ambiguous `linux-x86_64` and `linux-aarch64` classifiers;
update explicit classifier dependencies when upgrading. Other platforms keep
`windows-x86_64`, `windows-aarch64`, `macos-x86_64`, and `macos-arm64`.

Linux requires OpenSSL 3 (on Alpine, `apk add libssl3`); the C++ runtime is
linked in. Installing `gcompat` is unnecessary. The bundle
selects libc from `/proc/self/maps`; if procfs is unavailable, explicitly set
`-Dlibdatachannel.libc=glibc` or `-Dlibdatachannel.libc=musl` to match the JVM.

Build one Linux native with, for example:

```sh
./gradlew compileNativeForLinuxMuslAarch64 \
  -Plibdatachannel.targets=linux-musl-aarch64 \
  -Plibdatachannel.build-release-binaries=true
```

The CI matrix builds all eight Linux, Windows, and macOS targets concurrently
on matching x86_64 or ARM64 runners. Only musl uses a container (Alpine 3.22);
glibc builds use GCC 12 and OpenSSL from Ubuntu 22.04, Windows builds use native
MSYS2 MINGW64/CLANGARM64 with static OpenSSL, and macOS builds use the host
toolchain. Install the corresponding build dependencies for local builds.

Every job checks lazy and eager loading of the packaged bundle in a matching
JVM. Windows checks exclude toolchain DLLs from PATH. Publishing waits for every
matrix job to pass. Docker is required only for the musl targets; local builds
of those targets need a matching host architecture or Docker/QEMU emulation.

### Offerer example

```java
var cfg = RTCConfiguration.of("stun.l.google.com:19302");
// try with resources to cleanup peer when done
try (var peer = RTCPeerConnection.createPeer(cfg)) {
    // when complete send sdp to remote peer
    peer.onGatheringStateChange((pc, state) -> {
        if (RTC_GATHERING_COMPLETE == state) {
            var sdp = pc.localDescription();
            System.out.println(sdp);
        }
    });
    // create data channel
    var channel = peer.createDataChannel("test");
    // wait for local sdp...
    // then set answer from remote peer
    peer.setAnswer(readInput());
    // register message callback
    channel.onMessage((c, message, size) -> System.out.println("Incoming message: " + new String(message)));
    // block until channel is closed
    CompletableFuture<Void> future = new CompletableFuture<>();
    channel.onClose(c -> future.completeAsync(() -> null));
    future.join();
}
```

## Android

For Android apps an additional module exists: `libdatachannel-java-android`, which has an Android archive (.aar file) as
its main artifact. This artifact contains the native components in the correct file layouts, such that the library works
without any additional initialization code. The `arch-detect` module will not work on Android without changes!

### Permissions

To use libdatachannel on Android, the following permissions are required:

* `android.permission.INTERNET`

## Examples

* Web Example: https://pschichtel.github.io/libdatachannel-java/ ([Source](example/web))

See [tests](#TODO) for more examples
