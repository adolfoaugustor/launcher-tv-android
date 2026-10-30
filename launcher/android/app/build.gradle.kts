plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "br.tematv.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.tematv.launcher"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

// Empacota cada pasta de sample-themes/ como app/build/sample-themes/<nome>.tematv (pacotes de exemplo).
val sampleThemeDirs = rootProject.file("sample-themes").listFiles { file -> file.isDirectory }.orEmpty().sortedBy { it.name }
val packSampleThemeTasks = sampleThemeDirs.map { themeDir ->
    val taskSuffix = themeDir.name.split('-').joinToString("") { part -> part.replaceFirstChar(Char::uppercase) }
    tasks.register<Zip>("packSampleTheme$taskSuffix") {
        from(themeDir)
        archiveFileName.set("${themeDir.name}.tematv")
        destinationDirectory.set(layout.buildDirectory.dir("sample-themes"))
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
}
tasks.register("packSampleThemes") {
    group = "tematv"
    description = "Gera os pacotes .tematv de exemplo em app/build/sample-themes/."
    dependsOn(packSampleThemeTasks)
}

dependencies {
    implementation(project(":theme-core"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
