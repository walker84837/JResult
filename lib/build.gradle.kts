plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.37.0"
}

group = "org.winlogon"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("org.winlogon", "jresult", project.version.toString())

    pom {
        name.set("JResult")
        description.set(
            "A lightweight Java library inspired by Rust's Result<T, E> to help eliminate " +
                "the need for exception handling in scenarios where a value may either succeed or fail.",
        )
        url.set("https://github.com/walker84837/JResult")
        inceptionYear.set("2025")

        licenses {
            license {
                name.set("BSD Zero Clause License")
                url.set("https://opensource.org/licenses/0BSD")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("winlogon")
                name.set("winlogon")
                email.set("walker84837 at gmail dot com")
                url.set("https://github.com/walker84837")
            }
        }

        scm {
            url.set("https://github.com/walker84837/JResult")
            connection.set("scm:git:git://github.com/walker84837/JResult.git")
            developerConnection.set("scm:git:ssh://git@github.com/walker84837/JResult.git")
        }
    }
}
