plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.goreecloud.gallery"
version = "0.1.0-dev"

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}
