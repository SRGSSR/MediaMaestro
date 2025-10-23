/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.dependency.analysis.gralde.plugin)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.dokka.javadoc) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlinx.binary.compatibility.validator)
    alias(libs.plugins.kotlinx.kover)
}

allprojects {
    pluginManager.apply("io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        basePath = projectDir.absolutePath
        buildUponDefaultConfig = true
        config.setFrom(rootDir.resolve("config/detekt.yml"))
        ignoredBuildTypes = listOf("release")
        parallel = true
    }

    afterEvaluate {
        if (pluginManager.hasPlugin("org.jetbrains.dokka")) {
            dokka {
                moduleVersion = providers.environmentVariable("VERSION_NAME").orElse("main")

                pluginsConfiguration.html {
                    customAssets.from(layout.settingsDirectory.file("config/dokka/logo-icon.svg"))
                    customStyleSheets.from(layout.settingsDirectory.file("config/dokka/media-maestro.css"))
                    footerMessage = "© SRG SSR"
                }

                dokkaSourceSets.findByName("main")?.apply {
                    includes.from(layout.settingsDirectory.file("docs/${this@allprojects.name}.md"))

                    externalDocumentationLinks.register("kotlinx.coroutines") {
                        url("https://kotlinlang.org/api/kotlinx.coroutines")
                        packageListUrl("https://kotlinlang.org/api/kotlinx.coroutines/package-list")
                    }

                    sourceLink {
                        localDirectory.set(layout.projectDirectory.dir("src"))
                        remoteUrl("https://github.com/SRGSSR/MediaMaestro/tree/${moduleVersion.get()}/${project.name}/src")
                    }
                }
            }
        }
    }
}

subprojects {
    pluginManager.apply("com.autonomousapps.dependency-analysis")
}

apiValidation {
    ignoredProjects.add("demo")
    // See https://github.com/Kotlin/binary-compatibility-validator/issues/74
    ignoredClasses.add("ch.srgssr.media.maestro.ComposableSingletons\$CastIconKt")
    ignoredClasses.add("ch.srgssr.media.maestro.ComposableSingletons\$MediaRouteChooserDialogKt")
    ignoredClasses.add("ch.srgssr.media.maestro.ComposableSingletons\$MediaRouteControllerDialogKt")
}

dokka {
    dokkaPublications.html {
        includes.from(layout.settingsDirectory.file("docs/${project.name}.md"))
    }
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }

            onUnusedDependencies {
                exclude(libs.androidx.compose.ui.tooling.asProvider())
            }
        }

        project(":demo") {
            onUnusedDependencies {
                exclude(libs.androidx.media3.exoplayer.dash)
            }
        }
    }
}

dependencies {
    dokka(project(":media-maestro"))

    kover(project(":media-maestro"))
}
