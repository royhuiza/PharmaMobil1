import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    id("app.cash.sqldelight") version "2.3.2"
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    android {
       namespace = "pe.edu.upeu.pharmamobil.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
            implementation("app.cash.sqldelight:android-driver:2.3.2")
            // api: MainApplication (androidApp) usa androidContext() al arrancar Koin.
            api(libs.koin.android)
        }
        commonMain.dependencies {
            // api: androidApp llama a initKoin(), cuya firma expone KoinAppDeclaration.
            api(libs.koin.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
tasks.withType<Test>().configureEach {
    testLogging {
        showStandardStreams = true
    }
}
tasks.register("diagnosticarSqlDelight") {
    doLast {
        println("SQLDelight aplicado: " +
            project.plugins.hasPlugin("app.cash.sqldelight"))

        println("Tareas SQLDelight: " +
            project.tasks.names.filter {
                it.contains("sqldelight", ignoreCase = true) ||
                it.contains("pharmadb", ignoreCase = true)
            })
    }
}



sqldelight {
    databases {
        register("PharmaDb") {
            packageName.set("pe.edu.upeu.pharmamobil.db")
        }
    }
}
tasks.register("verConfiguracionesAndroid") {
    doLast {
        project.configurations
            .filter {
                it.name.contains("android", ignoreCase = true) &&
                it.name.contains("compile", ignoreCase = true)
            }
            .forEach {
                println("${it.name} - resolvable: ${it.isCanBeResolved}")
            }
    }
}
