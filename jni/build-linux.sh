#!/bin/sh
set -eu

architecture=$(uname -m)
case "$TARGET_CLASSIFIER" in
    linux-glibc-"$architecture"|linux-musl-"$architecture")
        ;;
    *)
        echo "Build $TARGET_CLASSIFIER on a matching Linux host (found $architecture)" >&2
        exit 1
        ;;
esac
cmake -S jni -B "build/dockcross/$TARGET_CLASSIFIER/native" \
    -DCMAKE_POLICY_VERSION_MINIMUM=3.5 \
    -DCMAKE_BUILD_TYPE="$PROJECT_BUILD_TYPE" \
    -DPROJECT_VERSION="$PROJECT_VERSION" \
    -DSTATIC_LIBSTDCXX=ON
cmake --build "build/dockcross/$TARGET_CLASSIFIER/native" \
    --target datachannel-java --parallel "$JOBS"
