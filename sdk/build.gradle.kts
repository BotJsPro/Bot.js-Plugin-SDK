import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "org.botjs.plugin.sdk"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

    }

    // 这个 publishing 是 Android 的，只放 singleVariant
    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.11.0")
}

// 注意：在 android { } 外面，文件最顶层
afterEvaluate {
    extensions.configure<PublishingExtension>("publishing") {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.BotJsPro"
                artifactId = "Bot.js-Plugin-SDK"
                version = "1.0.0"
            }
        }
    }
}