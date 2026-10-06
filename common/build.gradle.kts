plugins {
    id("java-library")
}

dependencies {
    // No external dependencies - all platform-specific dependencies are in platform modules
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}