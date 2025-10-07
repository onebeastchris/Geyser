plugins {
    id("geyser.modded-conventions")
}

architectury {
    common("neoforge", "fabric")
}

loom {
    mixin.defaultRefmapName.set("geyser-refmap.json")
}

afterEvaluate {
    // We don't need these
    tasks.named("remapModrinthJar").configure {
        enabled = false
    }
}

dependencies {
    api(projects.core)
    compileOnly(libs.mixin)
    compileOnly(libs.mixinextras)

    // Only here to suppress "unknown enum constant EnvType.CLIENT" warnings. DO NOT USE!
    compileOnly(libs.fabric.loader)
}

// THIS IS BAD!
//tasks {
//    processResources {
//        from(project(":core").sourceSets.main.get().resources)
//    }
//}

// Why does it work here, but not in fabric???
//sourceSets {
//    main {
//        java {
//            srcDirs += project(":core").sourceSets.main.get().java.srcDirs
//        }
//        resources {
//            srcDirs += project(":core").sourceSets.main.get().resources.srcDirs
//        }
//    }
//}
