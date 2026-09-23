plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "io.github.badgersmc"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.frengor.com/repository/public/")
    maven("https://repo.artillex-studios.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    // Server-provided (not shaded)
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.frengor:ultimateadvancementapi:2.8.1")
    compileOnly(files("../luma-guilds/build/libs/LumaGuilds-2.1.0.jar"))
    compileOnly(files("../enthusia-market/build/libs/EnthusiaMarket-0.2.0.jar"))
    compileOnly("com.artillexstudios:AxKothAPI:4")
    compileOnly(files("../diary-keeper/target/DiaryKeeper-1.4.8.jar"))
    // NOTE: Project.files() does NOT expand glob patterns — files("../x/*.jar")
    // is treated as a literal (missing) path and silently yields an empty
    // classpath. fileTree() globs properly. Exact names drift with version
    // bumps (commend 2.x, currency 1.4.x), so glob via fileTree.
    compileOnly(fileTree("../enthusia-currency/target") { include("enthusia-currency-*.jar") })
    compileOnly(files("../playtime-plugin/target/playtime-plugin-3.5.16.jar"))
    // The commend artifact is "EnthusiaCommend" (capital E, no hyphen) — its
    // target jar is EnthusiaCommend-2.13.1.jar, not enthiusa-commend-*.jar.
    compileOnly(fileTree("../enthusia-commend/target") { include("EnthusiaCommend-*.jar") })

    // Koin DI — LumaGuilds shadows Koin into its jar, but EA needs the
    // API at compile-time to look up LumaGuilds services via GlobalContext.
    compileOnly("io.insert-koin:koin-core:4.0.2")

    // Shaded into JAR
    implementation("com.github.BadgersMC.Nexus:nexus-core:2.2.0")
    implementation("com.github.BadgersMC.Nexus:nexus-paper:2.2.0")
    implementation("com.typesafe:config:1.4.3")

    // Test — compileOnly deps need to be on test runtime classpath for mocking
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation("com.frengor:ultimateadvancementapi:2.8.1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("net.badgersmc.nexus", "io.github.badgersmc.advancements.lib.nexus")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
