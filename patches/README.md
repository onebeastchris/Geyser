# Patched libdatachannel-java build for GeyserMC/Geyser#6854

This branch is opencollab-incubator/libdatachannel-java at 2d7bc8a (submodules unchanged) plus:

- `patches/jni/libdatachannel/deps/usrsctp/0001-*.patch`: usrsctp restarts its T3-rtx timer when a SACK without
  gap blocks moves the cumulative ack (RFC 4960 6.3.2 R3). See the patch for the full explanation.
  `native-builds.yml` applies everything under `patches/<submodule>/` to that submodule after checkout.
- `publish-fork.yml`: on every push to this branch, builds all eight natives and publishes
  `dev.opencollab:libdatachannel-java(-arch-detect):0.24.6.1-fork-<commit>` to the `libdatachannel-maven` branch,
  readable at https://raw.githubusercontent.com/onebeastchris/Geyser/libdatachannel-maven/

Only for testing until the fix lands in opencollab-incubator's forks.
