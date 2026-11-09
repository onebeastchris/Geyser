# Native transport regressions

```sh
git submodule update --init --recursive
./gradlew :nativeTransportProbe --no-daemon --max-workers=2 -Plibdatachannel.java-compiler-version=17
```

The focused Linux x86_64 build uses JDK 17, CMake, a C/C++ compiler, system OpenSSL
development files and the `openssl` CLI. Library bytecode remains compatible with
Java 11. The normal dockcross build remains available for portable artifacts.
The Java probes reserve loopback UDP ports 49184 and 49195. Native mux tests also
reserve their documented ports; do not run competing listeners there.

`NativeTransportProbe` sends an initial STUN request exactly once, delays the
application decision, and checks that the retained request receives a response.
A forged STUN integrity value creates no peer or address mapping. Timeout and
listener closure cancel pending attempts; later decisions cannot create peers.
Handler and initializer failures, executor rejection and expired settings also
reject safely. An initializer can close its peer or wait for another thread to
close the listener without losing native ownership or deadlocking.

The full connection tests use explicit ICE usernames of 167, 178 and 256
characters, a supplied PEM certificate, and two data channels carrying 402
messages. Repeated STUN requests during approval produce one admission callback.
Subsequent transport traffic stays native. An incorrect client certificate
fingerprint fails DTLS before either channel opens. A separate case imports a
password-encrypted PEM key and checks the certificate fingerprint.

`NativeLoggingProbe` enables every Java log level, then verifies that native
filtering suppresses messages before JNI. It checks configuration before library
loading and changes after initialization. The default native threshold is
`WARNING`; `LibDataChannel.setLogLevel(...)` changes it for the process.

The suite also checks bounded transport teardown from an external owner thread
and forbids waiting inside native event callbacks. The native teardown test
stalls a worker and retains a transport reference, so completion cannot be
mistaken for task submission or handle removal. `CallbackCleanupProbe` closes
100 peers and verifies that all 300 peer/channel wrappers become collectible
without native invalid-handle errors. `CallbackDispatchProbe` closes peers while
libdatachannel delivers their Closed ICE state from its own thread, once with the
test build's dispatch delay between reading the user pointer and using it, once
with the Java listener still running. It runs with `-Xcheck:jni`, so a released
global reference fails the probe. To run the normal JNI lifecycle tests with
a focused binary, pass `-Plibdatachannel.test-native-path=/absolute/library.so`
to the Gradle `test` task.

## Incoming connections

`IceUdpMuxListener` delivers immutable username fragments and the source address.
The handler returns a `CompletionStage<Acceptance>`; a null acceptance rejects the
request. Native code retains the STUN bytes, handles duplicates and verifies
STUN integrity before constructing the peer. JNI uses only libdatachannel's
public C API.

```java
var listener = new IceUdpMuxListener(bindAddress, port, executor, request -> {
    var settings = validate(request.localUfrag(), request.remoteUfrag());
    request.completion().whenComplete((peer, error) -> recordOutcome(peer, error));
    return CompletableFuture.completedFuture(IceUdpMuxListener.Acceptance
        .builder(settings.remoteOffer(), settings.localPassword())
        .configuration(configuration)
        .identity(new DtlsIdentity(certificatePath, keyPath))
        .peerExecutor(executor)
        .initialize(peer -> installCallbacks(peer))
        .expiresAt(settings.expiresAt()).build());
});
```

The handler and initializer run through the supplied executor. A bounded internal
queue ensures even a direct executor does not run application code inside JNI
request dispatch. The initializer installs peer callbacks before native code
continues the retained request. Accepted peers belong to the caller. Close them
before closing the listener.

The default limit is 256 pending attempts and a five-second deadline. Both are
configurable, up to 4096 attempts and 30 seconds. An acceptance can also carry an
application expiry time, checked before peer preparation and before final
acceptance. Handler failures, overload and timeouts reject the individual attempt.
A failed prepared peer remains owned until native teardown completes; only then
does the request's completion stage fail. `failure()` reports an unexpected
cleanup failure, without treating ordinary admission rejection as listener failure.

`statistics()` returns immutable named counters for received datagrams, rejections,
ICE agents, mapped tuples, pending requests, notifications and duplicates. The old
positional `stats()` adapter and `Acceptance` constructors are deprecated and retained
for existing consumers.

`PeerConnection.closeAsync()` returns a `CompletionStage<Void>` after native transport
destruction and Java cleanup. It is safe from event callbacks and uses native completion
notification rather than blocking a Java worker on each closing peer. The blocking
`closeAndAwait(Duration)` convenience returns false on timeout without releasing
ownership. Repeated calls return true after cleanup completes.

Unprepared deadline/close cancellation releases the Java admission slot independently
of the application executor. Completion continuations run on the common completion
pool, so user code cannot block the deadline or JNI callback thread. An initializer
already running cannot be forcibly stopped: its prepared peer remains owned until
initialization returns and cleanup completes. Cancellation is checked before final
acceptance; native expiry also prevents stale attachment.

Return `Acceptance.reuse(existingPeer)` to approve another source tuple for a peer.
Native code checks the existing ICE credentials and retains its SDP, DTLS identity,
channels and caller ownership, including on failed attachment. Use the optional
expiry argument when the application approval has its own deadline. Applications
remain responsible for deciding whether an address change is allowed.

The certificate overload uses upstream `rtcConfiguration` PEM fields; explicit
ICE credentials use upstream `rtcSetLocalDescriptionEx`. Key provisioning and
rotation remain application policy. Peers created without a supplied identity
use native-generated certificates.

## Local packaging

From a clean committed tree, `scripts/package-development.sh [maven-directory]`
runs native and JVM regressions and writes an immutable local artifact under
`io.github.teamziax:libdatachannel-java:<native-version>.0-dev.<full-commit>`.
The classifier identifies the host libc and architecture, for example `linux-glibc-x86_64`. `provenance.json` records all three source
SHAs and artifact hashes. This binary targets the current host's system
OpenSSL/ABI; it is not a portable dockcross release. Nothing is uploaded.
Rebuild headers and JNI together after changing the pinned native version.

For local CMake experiments, `LIBDATACHANNEL_SOURCE_DIR` can select a separate
libdatachannel checkout. The Gradle probe and packaging tasks explicitly select
the pinned submodule and bundled libjuice again.

Construction-attempt diagnostics are package-private test instrumentation and require
`RTC_ENABLE_TEST_DIAGNOSTICS=ON` in the native build. Ordinary builds omit the counter.
The probe suite additionally covers stalled application executors, same-peer tuple
attachment, forged attachment, reentrant listener close, scoped C++ preparation and
asynchronous destruction completion.

## Persistent STUN observations

`IceUdpMuxListener.monitorStun(serverHost, serverPort)` creates an independently owned
`StunUdpMuxMonitor` on the listener's UDP binding. Close each monitor explicitly;
closing the listener leaves an existing monitor alive. A standalone constructor is
also available. See [native STUN monitoring](../jni/libdatachannel/docs/stun-monitoring.md)
for socket sharing, resolution and refresh behavior.

`binding(index)` returns an immutable `StunBinding`, or an empty result while that
server index is unavailable. A snapshot retains its mapping and counters after the
monitor updates or closes. `lastSuccessAge()` is empty until a successful response;
retained snapshots continue ageing using `System.nanoTime()`. A STUN response
identifies a mapping; it does not establish reachability from other endpoints.

`:nativeStunMonitorProbe` runs as part of `:nativeTransportProbe`. Local IPv4 and IPv6
responders check the initial observation, shared socket ownership, snapshot age,
server errors and concurrent read/close. It does not wait for periodic refreshes.

## Selected ICE candidates

`PeerConnection.selectedCandidatePair()` preserves the complete native candidate
strings. `CandidatePair.localTransport()` and `remoteTransport()` expose the
transport alongside the existing address and type accessors. Native reads use
bounded dynamic buffers instead of the previous fixed 256-byte buffers.
`:nativeCandidatePairBufferProbe` checks long candidates, growth between reads,
size limits and unavailable pairs; `:nativeTransportProbe` checks a selected pair
from an established connection.

Detailed [contributor and source attribution](../docs/contribution-provenance.md) is retained separately.
