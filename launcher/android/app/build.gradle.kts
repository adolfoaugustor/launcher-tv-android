plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Segredo de ativação do revendedor (feature 004): fica fora do repositório e entra no app só na compilação.
val activationSecretFile = providers.gradleProperty("tematv.activationSecretFile")
    .map { file(it) }
    .getOrElse(file("${System.getProperty("user.home")}/.config/tematv/activation-secret"))
if (!activationSecretFile.isFile) {
    throw GradleException(
        "Segredo de ativação não encontrado em $activationSecretFile. Gere com: " +
            "./gradlew :activation-tool:installDist && activation-tool/build/install/activation-tool/bin/activation-tool gerar-segredo",
    )
}
val activationSecret = activationSecretFile.readText().trim()

android {
    namespace = "br.tematv.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.tematv.launcher"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "ACTIVATION_SECRET", "\"$activationSecret\"")
    }

    buildFeatures {
        buildConfig = true
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
    implementation(project(":activation-core"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
