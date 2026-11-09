# Upstream patches for the usrsctp T3-rtx bug (#6854)

Three repositories, one fix. The patches stack through submodules:

| Order | Repository to fork | Branch | Commit | Base | What it does |
|---|---|---|---|---|---|
| 1 | [paullouisageneau/usrsctp](https://github.com/paullouisageneau/usrsctp) (libdatachannel's submodule; also applies to [sctplab/usrsctp](https://github.com/sctplab/usrsctp) master) | `fix-t3-restart-express-sack` | `624c6d4` | `fec583d` | The fix: restart T3-rtx in `sctp_express_handle_sack` when the cumulative ack advances |
| 2 | [opencollab-incubator/libdatachannel](https://github.com/opencollab-incubator/libdatachannel) | `fix-usrsctp-t3-restart` | `548dfd0` | `2e2d86a` | Points `deps/usrsctp` at commit 1 |
| 3 | [opencollab-incubator/libdatachannel-java](https://github.com/opencollab-incubator/libdatachannel-java) | `fix-usrsctp-t3-restart` | `23157ad` | `2d7bc8a` | Points `jni/libdatachannel` at commit 2 |
| (3b) | same | same | `ba648f9` | `23157ad` | **Fork only, don't PR**: publishes builds to a `maven-repo` branch |

The commit messages explain the bug. The short version: usrsctp only restarts its retransmission timer on SACKs
with gap blocks, so while data stays in flight it expires every 2 x RTO regardless of acks, and against a
peer that delays acks (dcSCTP, i.e. every Bedrock client) each expiry resends data and cuts cwnd to one MTU. See
`../README.md` for the reproduction and numbers.

## Pushing to your forks with the exact commits

The `.bundle` files hold the commits with their original hashes, so the submodule pointers stay valid.
`git am` on the `.patch` files would create new hashes, and commits 2 and 3 would then point at commits that
don't exist.

From a clone of each fork:

```
git fetch <path>/usrsctp/fix-t3-restart-express-sack.bundle fix-t3-restart-express-sack:fix-t3-restart-express-sack
git push origin fix-t3-restart-express-sack
```

Do the same for `libdatachannel/fix-usrsctp-t3-restart.bundle` and `libdatachannel-java/fix-usrsctp-t3-restart.bundle`,
in that order. For the libdatachannel-java PR, open it from `23157ad` (drop `ba648f9`).

Commits 2 and 3 point the submodule URLs at `onebeastchris/usrsctp` and `onebeastchris/libdatachannel`. That works
for testing, but upstream needs these URLs on repositories opencollab controls before merging:

1. opencollab-incubator forks usrsctp (or the fix lands in paullouisageneau/usrsctp), and commit 1 goes there.
2. In the libdatachannel PR, set the `deps/usrsctp` URL to that repository.
3. Merge the libdatachannel PR, then point the libdatachannel-java PR's `jni/libdatachannel` at the merged commit
   on opencollab-incubator/libdatachannel and keep its original URL.

## Where to PR

- **usrsctp**: [sctplab/usrsctp](https://github.com/sctplab/usrsctp) is the real upstream, and the same change applies
  to its master. paullouisageneau/usrsctp follows it, and opencollab could carry the fix in its own fork meanwhile.
  FreeBSD's kernel SCTP shares `sctp_indata.c`, so it has the same bug.
- **opencollab-incubator/libdatachannel**: submodule bump (commit 2, with the URL from step 2 above).
- **opencollab-incubator/libdatachannel-java**: submodule bump (commit 3).
- **CloudburstMC/Network**: bump `libdatachannel` in `gradle/libs.versions.toml` once libdatachannel-java publishes.
  Keep `minRetransmitTimeoutMs(400)`: it matches dcSCTP's `rto_min`, which the client and BDS use.
- **GeyserMC/Geyser**: bump `libdatachannel` in `gradle/libs.versions.toml`.

## Testing build in the meantime

The `libdatachannel-java-t3fix` branch of onebeastchris/Geyser is libdatachannel-java `2d7bc8a` plus the usrsctp
patch, applied at build time. Every push builds all eight natives with upstream's own `native-builds.yml` and
publishes `0.24.6.1-fork-<commit>` to the `libdatachannel-maven` branch. The `debug-nethernet-more` branch reads it
from there (see `build-logic/.../geyser.base-conventions.gradle.kts` and `gradle/libs.versions.toml`).
