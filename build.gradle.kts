import org.gradle.kotlin.dsl.support.serviceOf
import tel.schich.dockcross.execute.DockerRunner
import tel.schich.dockcross.execute.NonContainerRunner
import tel.schich.dockcross.execute.RemoteSshRunner
import tel.schich.dockcross.tasks.DockcrossRunTask
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    id("tel.schich.libdatachannel.convention.common")
    alias(libs.plugins.dockcross)
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}

fun extractLibDataChannelVersion(): String {
    val regex = """#define\s+RTC_VERSION\s+"([^"]+)"""".toRegex()
    val headerPath = project.layout.projectDirectory
        .file("jni/libdatachannel/include/rtc/version.h")
        .asFile.toPath()
    val headerContent = Files.readString(headerPath)
    val match = regex.find(headerContent) ?: return "unknown"

    return match.groupValues[1]
}

fun produceVersion(): String {
    val libDataChannelVersion = extractLibDataChannelVersion()
    val hasTags = project.providers.exec {
        commandLine("git", "tag")
    }.standardOutput.asText.get().trim().isNotEmpty()
    val defaultVersion = "$libDataChannelVersion.0-SNAPSHOT"
    if (!hasTags) {
        return defaultVersion
    }
    val describeOutput = project.providers.exec {
        commandLine("git", "describe", "--tags")
    }.standardOutput.asText.get().trim().removePrefix("v")

    val parts = describeOutput.split("-", limit = 2)
    val tagVersion = parts[0]
    return if (parts.size > 1) {
        if (tagVersion.startsWith(libDataChannelVersion)) {
            val versionParts = tagVersion.split('.').toMutableList()
            versionParts[versionParts.size - 1] = versionParts[versionParts.size - 1].toInt().inc().toString()
            versionParts.joinToString(".") + "-SNAPSHOT"
        } else {
            defaultVersion
        }
    } else {
        if (tagVersion.startsWith(libDataChannelVersion)) {
            tagVersion
        } else {
            throw GradleException("The version derived from the latest git tag is conflicting with libdatachannel!")
        }
    }
}

version = providers.gradleProperty("libdatachannel.development-version").getOrElse(produceVersion())
val isSnapshot = version.toString().endsWith("-SNAPSHOT")
description = "${project.name} is a binding to the libdatachannel that feels native to Java developers."

fun gitRevision(vararg command: String): String = providers.exec {
    commandLine(*command)
    isIgnoreExitValue = true
}.standardOutput.asText.map { it.trim() }.getOrElse("").ifEmpty { "unknown" }

// Recorded in the jar so consumers can rebuild exactly this chain, for example to get a native
// library with the test diagnostics that release builds leave out.
tasks.jar {
    manifest.attributes(
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version.toString(),
        "Source-Revision" to gitRevision("git", "rev-parse", "HEAD"),
        "LibDataChannel-Revision" to gitRevision("git", "rev-parse", "HEAD:jni/libdatachannel"),
        "LibJuice-Revision" to gitRevision("git", "-C", "jni/libdatachannel", "rev-parse", "HEAD:deps/libjuice"),
    )
}

val currentVersion = tasks.register<DefaultTask>("currentVersion") {
    doLast {
        println(version)
    }
}

val archDetectConfiguration = configurations.register(Constants.ARCH_DETECT_CONFIG) {
    isCanBeConsumed = true
}

val androidConfiguration = configurations.register(Constants.ANDROID_CONFIG) {
    isCanBeConsumed = true
}

val jniPath = project.layout.projectDirectory.dir("jni")
tasks.compileJava.configure {
    val annotationProcessorArgs = listOf(
        "generate.jni.headers" to "true",
        "generate.cache.mode.default" to "EAGER_PERSISTENT",
    ).map { "-A${it.first}=${it.second}" }
    options.compilerArgs.addAll(annotationProcessorArgs)
    options.headerOutputDirectory = jniPath.dir("generated")
}

val nativeGroup = "native"
val ci = System.getenv("CI") != null
val buildReleaseBinaries = project.findProperty("libdatachannel.build-release-binaries")
    ?.toString()
    ?.ifEmpty { null }
    ?.toBooleanStrictOrNull()
    ?: !project.version.toString().endsWith("-SNAPSHOT")

fun DockcrossRunTask.configureSshRemoteBuild(target: BuildTarget) {
    if (!ci) {
        return
    }

    fun findTarget(scope: String?): URI? {
        return project.findProperty("libdatachannel${scope?.let { ".$it" }.orEmpty()}.ssh-target")
            ?.toString()
            ?.let(::URI)
    }

    val sshTarget = findTarget(scope = target.classifier)
        ?: findTarget(scope = target.family)
        ?: findTarget(scope = null)
        ?: return

    val sshRunner = RemoteSshRunner(sshTarget) {
        add("--exclude", "**/.git")
        // this intentionally only includes the root-.gitignore, so that the generated jni files are included
        add("--filter=.- .gitignore")
    }
    runner(sshRunner)
    image = ""
}

fun DockcrossRunTask.baseConfigure(outputTo: Directory, target: BuildTarget) {
    group = nativeGroup
    dockcrossTag = "20250109-7bf589c"

    inputs.file(project.layout.projectDirectory.file("jni/build.sh"))

    when {
        target.image == null -> {
            image = "dummy"
        }
        '/' in target.image || ':' in target.image -> {
            val parts = target.image.split(':', limit = 2)
            dockcrossRepository = parts[0]
            dockcrossTag = if (parts.size == 2) {
                parts[1]
            } else {
                "latest"
            }
            image = "dummy"
        }
        else -> {
            image = target.image
        }
    }

    inputs.dir(jniPath)

    dependsOn(tasks.compileJava)

    output = outputTo.dir("native")
    extraEnv.putAll(target.env)
    extraEnv.put("JOBS", project.gradle.startParameter.maxWorkerCount.toString())
    extraEnv.put("RELATIVE_PROJECT_PATH", output.get().asFile.toPath().relativize(jniPath.asFile.toPath()).toString())
    extraEnv.put("PROJECT_VERSION", project.version.toString())
    extraEnv.put("PROJECT_BUILD_TYPE", if (buildReleaseBinaries) "Release" else "Debug")
    extraEnv.put("TARGET_FAMILY", target.family)
    extraEnv.put("TARGET_CLASSIFIER", target.classifier)
    target.image?.let {
        extraEnv.put("TARGET_IMAGE", it)
    }

    script = listOf(listOf("bash", "../../../../jni/build.sh"))

    configureSshRemoteBuild(target)
}

fun Jar.baseConfigure(compileTask: TaskProvider<out Task>, buildOutputDir: Directory) {
    group = nativeGroup

    dependsOn(compileTask)

    from(buildOutputDir) {
        include("native/libdatachannel-java.so")
        include("native/libdatachannel-java.dll")
        include("native/libdatachannel-java.dylib")
    }
}

val dockcrossOutputDir: Directory = project.layout.buildDirectory.get().dir("dockcross")
val nativeForHostOutputDir: Directory = dockcrossOutputDir.dir("host")
val compileNativeForHost = tasks.register<DockcrossRunTask>("compileNativeForHost") {
    baseConfigure(nativeForHostOutputDir, BuildTarget(image = null, family = "host", classifier = "host"))
    unsafeWritableMountSource = true
    runner(NonContainerRunner)
}

val packageNativeForHost = tasks.register<Jar>("packageNativeForHost") {
    baseConfigure(compileNativeForHost, nativeForHostOutputDir)
    archiveClassifier = "host"
}

data class BuildTarget(
    val image: String?,
    val family: String,
    val classifier: String,
    val nativeLinuxContainer: Boolean = false,
    val env: Map<String, String> = emptyMap(),
    val args: List<String> = emptyList(),
    val outputTo: NamedDomainObjectProvider<Configuration> = archDetectConfiguration,
)

fun androidTarget(abi: String) = BuildTarget(
    image = "ghcr.io/pschichtel/cross-build/android:20250620-f1c8ddd",
    family = "android",
    classifier = "${Constants.ANDROID_CLASSIFIER_PREFIX}$abi",
    env = mapOf("ANDROID_ABI" to abi),
    outputTo = androidConfiguration,
)

fun macosTarget(classifier: String, arch: String) = BuildTarget(
    image = "ghcr.io/pschichtel/cross-build/osx:20250620-f1c8ddd",
    family = "macos",
    classifier = "${Constants.MACOS_CLASSIFIER_PREFIX}$classifier",
    env = mapOf("OSXCROSS_HOST" to "$arch-apple-darwin23.6"),
)

val allTargets = listOf(
    BuildTarget(
        image = null,
        family = "linux",
        classifier = "${Constants.LINUX_CLASSIFIER_PREFIX}glibc-x86_64",
    ),
//    BuildTarget(
//        image = "linux-x86",
//        family = "linux",
//        classifier = "${Constants.LINUX_CLASSIFIER_PREFIX}x86_32",
//    ),
    BuildTarget(
        image = null,
        family = "linux",
        classifier = "${Constants.LINUX_CLASSIFIER_PREFIX}glibc-aarch64",
    ),
    BuildTarget(
        image = "alpine:3.22",
        family = "linux",
        classifier = "linux-musl-x86_64",
        nativeLinuxContainer = true,
    ),
    BuildTarget(
        image = "alpine:3.22",
        family = "linux",
        classifier = "linux-musl-aarch64",
        nativeLinuxContainer = true,
    ),
    BuildTarget(
        image = "windows-static-x64",
        family = "windows",
        classifier = "${Constants.WINDOWS_CLASSIFIER_PREFIX}x86_64",
    ),
    // dockcross has no static arm64 image, the llvm-mingw runtime is linked in by -static instead.
    // Conan's mingw fallback for armv8 is the generic mingw-common, which carries no arm64
    // assembly, so OpenSSL's own target for this architecture is picked explicitly.
    BuildTarget(
        image = "windows-arm64",
        family = "windows",
        classifier = "${Constants.WINDOWS_CLASSIFIER_PREFIX}aarch64",
        env = mapOf("CONAN_OPENSSL_CONFIGURATION" to "mingwarm64"),
    ),
//    BuildTarget(
//        image = "windows-static-x86",
//        family = "windows",
//        classifier = "${Constants.WINDOWS_CLASSIFIER_PREFIX}x86_32",
//    ),
    androidTarget(abi = "armeabi-v7a"),
    androidTarget(abi = "arm64-v8a"),
    androidTarget(abi = "x86"),
    androidTarget(abi = "x86_64"),
    macosTarget(classifier = "arm64", arch = "aarch64"),
    macosTarget(classifier = "x86_64", arch = "x86_64"),
)

// Optional comma separated list of target classifiers, defaults to all targets.
val targets = providers.gradleProperty("libdatachannel.targets")
    .map { spec ->
        val requested = spec.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        val known = allTargets.map { it.classifier }.toSet()
        val unknown = requested - known
        if (unknown.isNotEmpty()) {
            throw GradleException("Unknown targets: ${unknown.sorted().joinToString()}, known targets: ${known.sorted().joinToString()}")
        }
        allTargets.filter { it.classifier in requested }
    }
    .getOrElse(allTargets)

val packageNativeAll = tasks.register<DefaultTask>("packageNativeAll") {
    group = nativeGroup
}

var previousCompileNative: TaskProvider<DockcrossRunTask>? = null
for (target in targets) {
    val outputDir: Directory = dockcrossOutputDir.dir(target.classifier)
    val taskSuffix = target.classifier.split("[_-]".toRegex())
        .joinToString(separator = "") { it.lowercase().replaceFirstChar(Char::uppercaseChar) }
    val prebuiltPath = project.findProperty("libdatachannel.${target.classifier}.prebuilt-path")
        ?.toString()
        ?.ifBlank { null }
        ?.let { Paths.get(it).toFile() }

    val packageTaskName = "packageNativeFor$taskSuffix"
    val packageNative = if (prebuiltPath == null) {
        val compileNative: TaskProvider<out Task>
        if (target.nativeLinuxContainer) {
            compileNative = tasks.register<Exec>("compileNativeFor$taskSuffix") {
                group = nativeGroup
                dependsOn(tasks.compileJava)
                inputs.dir(jniPath)
                inputs.property("version", project.version.toString())
                inputs.property("release", buildReleaseBinaries)
                inputs.property("image", target.image!!)
                outputs.file(outputDir.file("native/libdatachannel-java.so"))
                val architecture = if (target.classifier.endsWith("aarch64")) {
                    "arm64"
                } else {
                    "amd64"
                }
                commandLine("docker", "run", "--rm", "--platform", "linux/$architecture",
                    "-v", "${project.projectDir}:/work:z", "-w", "/work",
                    "-e", "TARGET_CLASSIFIER=${target.classifier}",
                    "-e", "PROJECT_VERSION=${project.version}",
                    "-e", "PROJECT_BUILD_TYPE=${if (buildReleaseBinaries) "Release" else "Debug"}",
                    "-e", "JOBS=${project.gradle.startParameter.maxWorkerCount}",
                    target.image!!, "sh", "jni/build-alpine.sh")
            }
        } else if (target.classifier.startsWith("linux-glibc-")) {
            compileNative = tasks.register<Exec>("compileNativeFor$taskSuffix") {
                group = nativeGroup
                dependsOn(tasks.compileJava)
                inputs.dir(jniPath)
                inputs.property("version", project.version.toString())
                inputs.property("release", buildReleaseBinaries)
                outputs.file(outputDir.file("native/libdatachannel-java.so"))
                environment("TARGET_CLASSIFIER", target.classifier)
                environment("PROJECT_VERSION", project.version.toString())
                environment("PROJECT_BUILD_TYPE", if (buildReleaseBinaries) "Release" else "Debug")
                environment("JOBS", project.gradle.startParameter.maxWorkerCount.toString())
                commandLine("sh", "jni/build-linux.sh")
            }
        } else if (target.family == "windows" && System.getProperty("os.name").startsWith("Windows")) {
            compileNative = tasks.register<Exec>("compileNativeFor$taskSuffix") {
                group = nativeGroup
                dependsOn(tasks.compileJava)
                inputs.dir(jniPath)
                inputs.property("version", project.version.toString())
                inputs.property("release", buildReleaseBinaries)
                outputs.file(outputDir.file("native/libdatachannel-java.dll"))
                environment("TARGET_CLASSIFIER", target.classifier)
                environment("PROJECT_VERSION", project.version.toString())
                environment("PROJECT_BUILD_TYPE", if (buildReleaseBinaries) "Release" else "Debug")
                environment("JOBS", project.gradle.startParameter.maxWorkerCount.toString())
                commandLine(providers.environmentVariable("MSYS2_BASH").getOrElse("bash"), "jni/build-windows.sh")
            }
        } else {
            val dockcrossCompile = tasks.register("compileNativeFor$taskSuffix", DockcrossRunTask::class) {
                baseConfigure(outputDir, target)
                unsafeWritableMountSource = true
                containerName = "dockcross-${project.name}-${target.classifier}"
            }


            if (ci) {
                val previous = previousCompileNative
                dockcrossCompile {
                    if (target.image == null) {
                        runner(NonContainerRunner)
                    } else {
                        runner(DockerRunner())
                    }
                    if (previous != null) {
                        mustRunAfter(previous)
                    }

                    if (target.image != null) {
                        val execOps = project.serviceOf<ExecOperations>()
                        doLast {
                            execOps.exec {
                                commandLine("docker", "image", "prune", "-af")
                            }
                        }
                    }
                }

                previousCompileNative = dockcrossCompile
            }
            compileNative = dockcrossCompile
        }

        tasks.register(packageTaskName, Jar::class) {
            baseConfigure(compileNative, outputDir)
            archiveClassifier = target.classifier
        }
    } else {
        tasks.register(packageTaskName, Jar::class) {
            group = nativeGroup
            from(prebuiltPath.parentFile) {
                include(prebuiltPath.name)
                includeEmptyDirs = false
                eachFile {
                    val fileName = when (target.family) {
                        "macos" -> "libdatachannel-java.dylib"
                        "windows" -> "libdatachannel-java.dll"
                        else -> "libdatachannel-java.so"
                    }
                    relativePath = RelativePath.parse(true, "native/$fileName")
                }
            }
            archiveClassifier = target.classifier

            doFirst {
                if (!prebuiltPath.exists()) {
                    throw GradleException("Prebuilt binary for $target does not exist: $prebuiltPath")
                }
            }
        }
    }

    publishing.publications.withType<MavenPublication>().configureEach {
        artifact(packageNative)
    }

    packageNativeAll.configure {
        dependsOn(packageNative)
    }

    artifacts.add(target.outputTo.name, packageNative)
}

dependencies {
    annotationProcessor(libs.jniAccessGenerator)
    compileOnly(libs.jniAccessGenerator)

    if (providers.gradleProperty("libdatachannel.test-native-path").isPresent) {
        // Package the selected binary on the classpath so child JVM lifecycle probes
        // load the same checkout's library without relying on inherited properties.
        val focusedNative = tasks.register<Jar>("packageNativeForFocusedTests") {
            dependsOn("compileNativeProbe")
            archiveFileName = "focused-test-native.jar"
            destinationDirectory = layout.buildDirectory.dir("focused-test-native")
            from(providers.gradleProperty("libdatachannel.test-native-path")) {
                into("native")
                rename { "libdatachannel-java.so" }
            }
        }
        testImplementation(files(focusedNative))
    } else {
        testImplementation(files(packageNativeForHost))
    }

    testRuntimeOnly(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiter)
}

tasks.test {
    useJUnitPlatform()
}

publishing.publications.withType<MavenPublication>().configureEach {
    pom {
        description = "${project.description}"
    }
}

val openCollabDeploy = tasks.register<DefaultTask>("openCollabDeploy") {
    group = "publishing"

    val repo = if (isSnapshot) {
        Constants.SNAPSHOTS_REPO
    } else {
        Constants.RELEASES_REPO
    }
    val hasAndroidTargets = targets.any { it.family == "android" }
    for (project in allprojects) {
        // the android module bundles the android natives, publishing it without them makes no sense
        if (!hasAndroidTargets && project.name.endsWith("-android")) {
            continue
        }
        val publishTasks = project.tasks
            .withType<PublishToMavenRepository>()
            .matching { it.repository.name == repo }
        dependsOn(publishTasks)
    }

    doFirst {
        logger.lifecycle("Deploying $version to $repo!")
    }
}

val githubActions = tasks.register<DefaultTask>("githubActions") {
    group = "publishing"
    val deployRefPattern = """^refs/(?:tags/v\d+\.\d+\.\d+\.\d+|heads/main)$""".toRegex()
    val ref = System.getenv("GITHUB_REF")?.ifBlank { null }?.trim()

    dependsOn(tasks.check)

    if (System.getenv("GITHUB_REPOSITORY") == "opencollab-incubator/libdatachannel-java" && ref != null && deployRefPattern.matches(ref)) {
        logger.lifecycle("Job in $ref will deploy!")
        dependsOn(openCollabDeploy)
    } else {
        logger.lifecycle("Job will only build!")
        dependsOn(tasks.assemble)
    }
}

// Focused local JNI transport tests using the existing library/package format.
// Uses system OpenSSL; the established dockcross release path remains available.
val configureNativeProbe by tasks.registering(Exec::class) {
    dependsOn(tasks.compileJava)
    commandLine("cmake", "-S", "jni", "-B", "build/native-probe", "-DCMAKE_POLICY_VERSION_MINIMUM=3.5",
        "-DLIBDATACHANNEL_SOURCE_DIR=${project.file("jni/libdatachannel").absolutePath}", "-DUSE_SYSTEM_JUICE=OFF",
        "-DCMAKE_BUILD_TYPE=Debug", "-DPROJECT_VERSION=${project.version}", "-DENABLE_LOCALHOST_ADDRESS=ON",
        "-DTRANSPORT_TEARDOWN_TESTS=ON", "-DPENDING_MUX_TESTS=ON", "-DICE_UDP_MUX_TESTS=ON",
        "-DRTC_ENABLE_TEST_DIAGNOSTICS=ON")
}
val compileNativeProbe by tasks.registering(Exec::class) {
    dependsOn(configureNativeProbe)
    commandLine("cmake", "--build", "build/native-probe", "--target", "datachannel-java", "transport-teardown-test", "ice-udp-mux-pending-test", "candidate-pair-buffer-test", "mux-pending-test", "mux-pending-lifetime-test", "mux-authentication-test", "mux-direct-test", "mux-admission-test", "mux-proxy-protocol-test", "ice-attribute-limits-test", "-j2")
}
val probeSourceSet = sourceSets.create("nativeProbe") {
    java.srcDir("native-test")
    compileClasspath += sourceSets.main.get().output + configurations.compileClasspath.get()
    runtimeClasspath += sourceSets.main.get().output + configurations.runtimeClasspath.get()
}
dependencies {
    add(probeSourceSet.implementationConfigurationName, libs.logbackClassic)
}
tasks.named<JavaCompile>(probeSourceSet.compileJavaTaskName) {
    javaCompiler = javaToolchains.compilerFor { languageVersion = JavaLanguageVersion.of(17) }
    options.release = 17
}
val probeIdentity by tasks.registering(Exec::class) {
    val dir = layout.buildDirectory.dir("probe-identity")
    outputs.dir(dir)
    doFirst { dir.get().asFile.mkdirs() }
    commandLine("openssl", "req", "-x509", "-newkey", "ec", "-pkeyopt", "ec_paramgen_curve:prime256v1",
        "-nodes", "-days", "1", "-subj", "/CN=native-probe", "-keyout", "build/probe-identity/key.pem", "-out", "build/probe-identity/cert.pem")
}
val probeEncryptedIdentity by tasks.registering(Exec::class) {
    dependsOn(probeIdentity)
    inputs.file("build/probe-identity/key.pem")
    outputs.file("build/probe-identity/key-encrypted.pem")
    commandLine("openssl", "pkcs8", "-topk8", "-in", "build/probe-identity/key.pem",
        "-out", "build/probe-identity/key-encrypted.pem", "-v2", "aes-256-cbc", "-passout", "pass:test-only-password")
}
val runTransportNativeTests by tasks.registering(Exec::class) {
    dependsOn(compileNativeProbe)
    commandLine("ctest", "--test-dir", "build/native-probe/libdatachannel", "--output-on-failure", "-R", "transport.teardown|mux.pending|mux.authentication|mux.direct|mux.admission|mux.proxy.protocol|ice.attribute.limits")
}
tasks.register<JavaExec>("nativeTransportProbe") {
    dependsOn(runTransportNativeTests, probeIdentity, probeEncryptedIdentity, tasks.named(probeSourceSet.classesTaskName), "nativeCallbackCleanupProbe", "nativeCallbackDispatchProbe", "nativeLoggingProbe", "nativeStunMonitorProbe", "nativeCandidatePairBufferProbe")
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    classpath = probeSourceSet.runtimeClasspath
    mainClass = "tel.schich.libdatachannel.NativeTransportProbe"
    systemProperty("libdatachannel.native.datachannel-java.path", layout.buildDirectory.file("native-probe/libdatachannel-java.so").get().asFile.absolutePath)
    args("build/probe-identity/cert.pem", "build/probe-identity/key.pem", "build/probe-identity/key-encrypted.pem")
}

tasks.register<Exec>("nativeCandidatePairBufferProbe") {
    dependsOn(compileNativeProbe)
    commandLine(layout.buildDirectory.file("native-probe/candidate-pair-buffer-test").get().asFile.absolutePath)
}

tasks.register<JavaExec>("nativeCallbackCleanupProbe") {
    dependsOn(compileNativeProbe, tasks.named(probeSourceSet.classesTaskName))
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    classpath = probeSourceSet.runtimeClasspath
    mainClass = "tel.schich.libdatachannel.CallbackCleanupProbe"
    systemProperty("libdatachannel.native.datachannel-java.path", layout.buildDirectory.file("native-probe/libdatachannel-java.so").get().asFile.absolutePath)
}

tasks.register<JavaExec>("nativeCallbackDispatchProbe") {
    dependsOn(compileNativeProbe, tasks.named(probeSourceSet.classesTaskName))
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    classpath = probeSourceSet.runtimeClasspath
    mainClass = "tel.schich.libdatachannel.CallbackDispatchProbe"
    // Turns a released global reference into a fatal error instead of whatever the heap holds
    jvmArgs("-Xcheck:jni")
    systemProperty("libdatachannel.native.datachannel-java.path", layout.buildDirectory.file("native-probe/libdatachannel-java.so").get().asFile.absolutePath)
}

tasks.register<JavaExec>("nativeLoggingProbe") {
    dependsOn(compileNativeProbe, tasks.named(probeSourceSet.classesTaskName))
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    classpath = probeSourceSet.runtimeClasspath
    mainClass = "tel.schich.libdatachannel.NativeLoggingProbe"
    systemProperty("libdatachannel.native.datachannel-java.path", layout.buildDirectory.file("native-probe/libdatachannel-java.so").get().asFile.absolutePath)
}

tasks.register<JavaExec>("nativeStunMonitorProbe") {
    dependsOn(compileNativeProbe, tasks.named(probeSourceSet.classesTaskName))
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(17) }
    classpath = probeSourceSet.runtimeClasspath
    mainClass = "tel.schich.libdatachannel.NativeStunMonitorProbe"
    systemProperty("libdatachannel.native.datachannel-java.path", layout.buildDirectory.file("native-probe/libdatachannel-java.so").get().asFile.absolutePath)
}

// A portable classpath for running the shipped bundle inside target JVMs.
tasks.register<Sync>("prepareNativeSmoke") {
    dependsOn(tasks.named(probeSourceSet.classesTaskName))
    from(tasks.jar)
    from(project(":libdatachannel-java-arch-detect").tasks.named("jar"))
    from(configurations[probeSourceSet.runtimeClasspathConfigurationName])
    from(probeSourceSet.output) {
        into("classes")
    }
    into(layout.buildDirectory.dir("native-smoke"))
}
