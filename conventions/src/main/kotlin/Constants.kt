object Constants {
    const val ARCH_DETECT_CONFIG = "archDetectConfiguration"
    const val ANDROID_CONFIG = "androidConfiguration"
    const val ANDROID_CLASSIFIER_PREFIX = "android-"
    const val LINUX_CLASSIFIER_PREFIX = "linux-"
    const val WINDOWS_CLASSIFIER_PREFIX = "windows-"
    const val MACOS_CLASSIFIER_PREFIX = "macos-"

    const val SNAPSHOTS_REPO = "openCollabSnapshots"
    const val RELEASES_REPO = "openCollabReleases"

    val CI = System.getenv("CI") != null
}