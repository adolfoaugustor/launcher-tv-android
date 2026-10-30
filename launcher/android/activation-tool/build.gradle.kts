plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("br.tematv.activationtool.MainKt")
    applicationName = "activation-tool"
}

dependencies {
    implementation(project(":activation-core"))
}
