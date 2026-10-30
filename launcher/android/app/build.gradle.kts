import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Segredo de ativação do revendedor (feature 004): fica fora do repositório e entra no app só na compilação.
val activationSecretFile = providers.gradleProperty("tematv.activationSecretFile")
    .map { file(it) }
    .getOrElse(file("${System.getProperty("user.home")}/.config/tematv/activation-secret"))
// Sem o segredo, só a compilação do app falha: as ferramentas do notebook (inclusive a que gera o segredo) continuam compilando.
val activationSecret = if (activationSecretFile.isFile) activationSecretFile.readText().trim() else ""
if (activationSecret.isEmpty()) {
    tasks.named("preBuild") {
        doFirst {
            throw GradleException(
                "Segredo de ativação não encontrado em $activationSecretFile. Gere com: " +
                    "./gradlew :activation-tool:installDist && activation-tool/build/install/activation-tool/bin/activation-tool gerar-segredo",
            )
        }
    }
}

// Chave definitiva de assinatura (feature 006): fora do repositório; o código do aparelho depende dela.
val signingPropertiesFile = file("${System.getProperty("user.home")}/.config/tematv/assinatura.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.isFile) signingPropertiesFile.inputStream().use { load(it) }
}
if (signingProperties.isEmpty) {
    tasks.matching { it.name == "preReleaseBuild" }.configureEach {
        doFirst {
            throw GradleException("Chave de assinatura não encontrada em $signingPropertiesFile. Crie com: tools/gerar-chave-assinatura.sh")
        }
    }
}

android {
    namespace = "br.tematv.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "br.tematv.launcher"
        minSdk = 28
        targetSdk = 34
        versionCode = 7
        versionName = "0.6.1"
        buildConfigField("String", "ACTIVATION_SECRET", "\"$activationSecret\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        if (!signingProperties.isEmpty) {
            create("release") {
                storeFile = file(signingProperties.getProperty("storeFile"))
                storePassword = signingProperties.getProperty("storePassword")
                keyAlias = signingProperties.getProperty("keyAlias")
                keyPassword = signingProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
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
