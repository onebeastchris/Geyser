#!/bin/sh
set -eu

# Only musl needs a container: keep the native compiler and libc in Alpine.
apk add --no-cache build-base cmake openssl-dev linux-headers su-exec
build_owner=$(stat -c '%u:%g' jni/generated)
exec su-exec "$build_owner" sh jni/build-linux.sh
