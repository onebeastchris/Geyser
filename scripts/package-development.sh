#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
# An explicit destination is a local Maven repository. This never uploads.
output=${1:-build/development-maven}
output=$(realpath -m "$output")
if test -n "$(git status --porcelain --untracked-files=normal)"; then
  echo 'Refusing an immutable development version from a dirty checkout' >&2
  exit 1
fi
revision=$(git rev-parse HEAD)
native_version=$(sed -n 's/^#define RTC_VERSION "\([^"]*\)"/\1/p' jni/libdatachannel/include/rtc/version.h)
version="${native_version}.0-dev.${revision}"
./gradlew :classes :nativeTransportProbe :test --no-daemon --max-workers=2 --no-configuration-cache \
  -Plibdatachannel.development-version="$version" \
  -Plibdatachannel.test-native-path="$PWD/build/native-probe/libdatachannel-java.so"
python3 - "$output" "$version" "$revision" <<'PY'
import hashlib, json, pathlib, platform, subprocess, sys, zipfile
output, version, revision = pathlib.Path(sys.argv[1]), sys.argv[2], sys.argv[3]
root = output / 'dev/opencollab/libdatachannel-java' / version
root.mkdir(parents=True, exist_ok=True)
architecture = platform.machine()
if architecture not in {"x86_64", "aarch64"}:
    raise SystemExit(f"Unsupported development architecture: {architecture}")
mappings = pathlib.Path('/proc/self/maps').read_text()
libc = 'musl' if 'ld-musl-' in mappings or 'libc.musl-' in mappings else 'glibc'
classifier = f'linux-{libc}-{architecture}'

def add(jar, path, name):
    info = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = 0o100644 << 16
    jar.writestr(info, path.read_bytes())
with zipfile.ZipFile(root / f'libdatachannel-java-{version}.jar', 'w') as jar:
    for folder in [pathlib.Path('build/classes/java/main'), pathlib.Path('build/resources/main')]:
        if folder.exists():
            for path in sorted(folder.rglob('*')):
                if path.is_file(): add(jar, path, path.relative_to(folder).as_posix())
with zipfile.ZipFile(root / f'libdatachannel-java-{version}-{classifier}.jar', 'w') as jar:
    add(jar, pathlib.Path('build/native-probe/libdatachannel-java.so'), 'native/libdatachannel-java.so')
(root / f'libdatachannel-java-{version}.pom').write_text(f'''<project xmlns="http://maven.apache.org/POM/4.0.0"><modelVersion>4.0.0</modelVersion>
<groupId>dev.opencollab</groupId><artifactId>libdatachannel-java</artifactId><version>{version}</version>
<licenses><license><name>Mozilla Public License 2.0</name><url>https://www.mozilla.org/MPL/2.0/</url></license></licenses>
<dependencies><dependency><groupId>org.slf4j</groupId><artifactId>slf4j-api</artifactId><version>2.0.17</version></dependency></dependencies>
</project>\n''')
def head(path):
    return subprocess.check_output(['git', '-C', path, 'rev-parse', 'HEAD'], text=True).strip()
provenance = {
    'coordinates': f'dev.opencollab:libdatachannel-java:{version}',
    'bindingRevision': revision,
    'libdatachannelRevision': head('jni/libdatachannel'),
    'libjuiceRevision': head('jni/libdatachannel/deps/libjuice'),
    'platform': classifier,
    'nativeBuild': 'system OpenSSL, Debug, current host ABI; not a portable release',
    'checks': ['nativeTransportProbe', 'nativeCallbackCleanupProbe', 'nativeLoggingProbe', 'test'],
    'sha256': {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(root.iterdir()) if path.is_file()},
}
(root / 'provenance.json').write_text(json.dumps(provenance, indent=2) + '\n')
print(root)
PY
