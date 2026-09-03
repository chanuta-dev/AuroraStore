/*
 * SPDX-FileCopyrightText: 2020-2024 Aurora OSS
 * SPDX-FileCopyrightText: 2023-2024 The Calyx Institute
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

// Signing configuration
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

val tokenUsername: String? = localProperties.getProperty("ossrhUsername")
val tokenPassword: String? = localProperties.getProperty("ossrhPassword")
val shouldSignRelease: Boolean
    get() = !tokenUsername.isNullOrEmpty() && !tokenPassword.isNullOrEmpty()

// Bump this version when making a new release
val libVersion = "3.6.4"

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.protobuf)
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

android {
    namespace = "com.aurora.gplayapi"
    compileSdk = 36
    version = libVersion

    defaultConfig {
        minSdk = 21
        aarMetadata {
            minCompileSdk = 21
        }
        consumerProguardFiles("proguard-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    packaging {
        resources {
            excludes += "**/*.proto"
        }
    }
}

dependencies {

    implementation(libs.google.gson)
    implementation(libs.google.protobuf)
    implementation(libs.squareup.okhttp)
    implementation(libs.jetbrains.kotlin.coroutines)
    implementation(libs.jetbrains.kotlin.serialization)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${libs.versions.protobufJavalite.get()}"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

// Run "./gradlew publishReleasePublicationToLocalRepository" to generate release AAR locally
publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.auroraoss"
            artifactId = "gplayapi"
            version = libVersion
            afterEvaluate {
                from(components["release"])
            }
            pom {
                name.set("GPlay API")
                description.set("A reversed engineered Google Play Store API")
                url.set("https://gitlab.com/AuroraOSS/gplayapi")

                licenses {
                    license {
                        name.set("GNU GENERAL PUBLIC LICENSE, Version 3")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }

                scm {
                    url.set("https://gitlab.com/AuroraOSS/gplayapi")
                    connection.set("scm:git:git@gitlab.com:AuroraOSS/gplayapi.git")
                    developerConnection.set("scm:git:git@gitlab.com:AuroraOSS/gplayapi.git")
                }

                developers {
                    developer {
                        id.set("whyorean")
                        name.set("Rahul Patel")
                        email.set("whyorean@gmail.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitLab"
            version = libVersion
            url = uri("https://gitlab.com/api/v4/projects/18497829/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = System.getenv("CI_JOB_TOKEN")
            }
            authentication {
                create("header", HttpHeaderAuthentication::class)
            }
        }
        maven {
            name = "local"
            version = libVersion
            url = uri("./build/repo")
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}

tasks.withType<Sign> {
    onlyIf("Signing credentials are present (only used for maven central)") { shouldSignRelease }
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
