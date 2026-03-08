plugins {
    java
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("com.dirshortcut.Main")
}

repositories {
    // No external dependencies — standard library only
}

dependencies {
    // No third-party libraries
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.dirshortcut.Main"
    }
}
