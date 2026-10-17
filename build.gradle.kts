plugins {
    kotlin("jvm") version "2.3.20"
    id("com.gradleup.shadow") version "8.3.11"
}

group = "io.github.badgersmc"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.frengor.com/repository/public/")
    maven("https://jitpack.io")
}

dependencies {
    // Server-provided (not shaded)
    compileOnly("io.papermc.paper:paper-api:26.2.build.129-stable")
    compileOnly("com.frengor:ultimateadvancementapi:2.8.1")

    // LumaGuilds API: prefer an explicit artifact path from the monorepo/CI.
    // Otherwise resolve any current LumaGuilds jar instead of pinning a stale filename.
    val lumaGuildsJar = System.getenv("LUMAGUILDS_JAR")
        ?: project.findProperty("lumaguilds.jar")?.toString()
    if (lumaGuildsJar != null) {
        compileOnly(files(lumaGuildsJar))
    } else {
        compileOnly(fileTree("../luma-guilds/build/libs") { include("LumaGuilds-*.jar") })
    }

    // EnthusiaMarket API: prefer an explicit artifact path from the monorepo/CI.
    // Otherwise resolve any current Market jar instead of pinning a stale filename.
    val enthusiaMarketJar = System.getenv("ENTHUSIAMARKET_JAR")
        ?: project.findProperty("enthusiamarket.jar")?.toString()
    if (enthusiaMarketJar != null) {
        compileOnly(files(enthusiaMarketJar))
    } else {
        compileOnly(fileTree("../enthusia-market/build/libs") { include("EnthusiaMarket-*.jar") })
    }
    compileOnly(files("../diary-keeper/target/DiaryKeeper-1.4.8.jar"))
    // NOTE: Project.files() does NOT expand glob patterns — files("../x/*.jar")
    // is treated as a literal (missing) path and silently yields an empty
    // classpath. fileTree() globs properly. Exact names drift with version
    // bumps (commend 2.x, currency 1.4.x), so glob via fileTree.
    compileOnly(fileTree("../enthusia-currency/target") { include("enthusia-currency-*.jar") })
    val enthusiaPlaytimeJar = System.getenv("ENTHUSIAPLAYTIME_JAR")
        ?: project.findProperty("enthusiaplaytime.jar")?.toString()
    if (enthusiaPlaytimeJar != null) {
        compileOnly(files(enthusiaPlaytimeJar))
    } else {
        compileOnly(fileTree("../playtime-plugin/target") { include("playtime-plugin-*.jar") })
    }
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
    testImplementation("io.papermc.paper:paper-api:26.2.build.129-stable")
    testImplementation("com.frengor:ultimateadvancementapi:2.8.1")
    testImplementation(kotlin("test"))
    testImplementation(enforcedPlatform("org.junit:junit-bom:5.14.4"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v26.2:4.116.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    exclude("net/badgersmc/ek/api/**")
    archiveClassifier.set("")
    relocate("net.badgersmc.nexus", "io.github.badgersmc.advancements.lib.nexus")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
