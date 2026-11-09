#!/usr/bin/env bash

set -euo pipefail

set -x

export CONAN_HOME="${OUTPUT_DIR}/conan/home"
# OpenSSL's makefile constructs broken compiler paths due to CROSS_COMPILE
export CROSS_COMPILE=""
# Clearing CROSS_COMPILE also unprefixes ranlib, and the host one cannot index every target's
# archives, so it is derived from the cross ar instead
if [ -z "${RANLIB:-}" ] && [ -n "${AR:-}" ] && [ -x "${AR%ar}ranlib" ]
then
  export RANLIB="${AR%ar}ranlib"
fi

predefined_profile_path='/conan-profile.ini'
classifier_profile_path="${MOUNT_SOURCE}/jni/conan-profiles/$TARGET_CLASSIFIER.ini"
profile_path="${CONAN_HOME}/profiles/default"
mkdir -vp "$(dirname "$profile_path")"
if [ -e "$predefined_profile_path" ]
then
  cp -av "$predefined_profile_path" "$profile_path"
elif [ -e "$classifier_profile_path" ]
then
  cp -av "$classifier_profile_path" "$profile_path"
else
  conan profile detect -f
fi

cmake_options=(
  '-DCMAKE_POLICY_VERSION_MINIMUM=3.5'
  "-DCMAKE_PROJECT_TOP_LEVEL_INCLUDES=${MOUNT_SOURCE}/jni/cmake-conan/conan_provider.cmake"
  "-DPROJECT_VERSION=${PROJECT_VERSION}"
  "-DCMAKE_BUILD_TYPE=${PROJECT_BUILD_TYPE}"
  "-DENABLE_HARDENING=${ENABLE_HARDENING:-ON}"
)

if [ "$TARGET_FAMILY" = 'android' ]
then
  cmake_options+=(
    "-DANDROID_ABI=${ANDROID_ABI}"
    "-DANDROID_PLATFORM=android-21"
    "-DANDROID_STL=c++_static"
    "-DANDROID_NDK=${ANDROID_NDK_ROOT}"
  )
elif [ "$TARGET_FAMILY" = 'macos' ]
then
  cp -v "${MOUNT_SOURCE}/jni/conan-profiles/${TARGET_CLASSIFIER}.ini" "$profile_path"
  cmake_options+=(
    "-DOSXCROSS_HOST=${OSXCROSS_HOST}"
  )
elif [ "$TARGET_FAMILY" = 'windows' ] && "$CC" --version | grep -qi clang
then
  # llvm-mingw reports itself as clang, but conan only recognizes gcc as mingw, and OpenSSL keys
  # its Windows entry points off the target name conan derives from that
  compiler_major="$("$CC" -dumpversion | cut -d. -f1)"
  cmake_options+=(
    "-DCONAN_INSTALL_ARGS=--build=missing;-s:h;compiler=gcc;-s:h;compiler.version=${compiler_major};-s:h;compiler.libcxx=libstdc++11"
  )
fi

cmake "$RELATIVE_PROJECT_PATH" "${cmake_options[@]}"
make -j"${JOBS:-1}"
