group = "dev.anicanon.swiftjava"
version = "0.1.1"

allprojects {
    group = rootProject.group
    version = rootProject.version
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

// Publish every subproject's Maven publication (core, plugin, marker) to
// GitHub Packages so consumers resolve them from a fresh clone instead of an
// `includeBuild` of this checkout. Credentials from gpr.user/gpr.key locally
// or GITHUB_ACTOR/GITHUB_TOKEN in Actions; a no-op without credentials so a
// plain local build never fails for lack of a token.
subprojects {
    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            val gprUser = (findProperty("gpr.user") as String?) ?: System.getenv("GITHUB_ACTOR")
            val gprKey = (findProperty("gpr.key") as String?) ?: System.getenv("GITHUB_TOKEN")
            if (gprUser != null && gprKey != null) {
                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/AniCanon/swift-java-kotlin-accessors")
                        credentials {
                            username = gprUser
                            password = gprKey
                        }
                    }
                }
            }
        }
    }
}
