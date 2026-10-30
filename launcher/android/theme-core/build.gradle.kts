plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

tasks.test {
    // BundledThemesTest lê o tema padrão e os exemplos a partir da raiz do projeto Gradle.
    systemProperty("tematv.projectRoot", rootProject.projectDir.absolutePath)
}
