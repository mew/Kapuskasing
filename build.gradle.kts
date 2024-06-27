plugins {
    id("java")
}

group = "ca.noratastic"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-commons:9.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "ca.noratastic.kapuskasing.Main"
    }
    exclude("module-info.class")
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}