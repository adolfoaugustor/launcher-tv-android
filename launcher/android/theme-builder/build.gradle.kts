plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("br.tematv.themebuilder.MainKt")
    applicationName = "tematv"
    // Socket só IPv4: o encaminhamento de localhost do WSL2 não enxerga o IPv6 mapeado (::ffff:127.0.0.1) do Java.
    applicationDefaultJvmArgs = listOf("-Djava.net.preferIPv4Stack=true")
}

// Lista NVI do tema padrão e molduras dos exemplos: fonte única, copiada na compilação (D-06).
val copyPresets = tasks.register<Sync>("copyPresets") {
    into(layout.buildDirectory.dir("generated/presets"))
    from(rootProject.file("app/src/main/assets/default-theme/theme.json")) {
        into("presets")
        rename { "nvi.json" }
    }
    listOf("biblia", "hamburguer").forEach { presetId ->
        val sampleDir = rootProject.file("sample-themes/$presetId-exemplo")
        from(sampleDir) {
            include("frame-*.png")
            into("presets")
        }
        from(sampleDir.resolve("theme.json")) {
            into("presets")
            rename { "$presetId.json" }
        }
    }
}

sourceSets.main {
    resources.srcDir(copyPresets)
}

dependencies {
    implementation(project(":theme-core"))
    implementation(project(":activation-core"))
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}
