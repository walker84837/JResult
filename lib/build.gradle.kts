plugins {
    id("java-library")
    id("maven-publish")
}

group = "com.github.walker84837"
version = "1.1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        from(components["java"])
        groupId = project.group.toString()
        artifactId = project.name
        version = project.version.toString()
    }
}

tasks.publishToMavenLocal {
    dependsOn(tasks.assemble)
}
