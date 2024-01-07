plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenLocal()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://ci.ender.zone/plugin/repository/everything/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.dmulloy2.net/repository/releases/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jcenter.bintray.com")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io")
    maven("https://nexus.wesjd.net/repository/thirdparty/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.mikeprimm.com/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://repo.codemc.io/repository/maven-releases/")

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    mavenCentral()
}

dependencies {
    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    // Adventure
    implementation("net.kyori:adventure-api:4.15.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.15.0")

    // Other
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("net.wesjd:anvilgui:1.9.2-SNAPSHOT")
    implementation("co.aikar:fastutil-base:3.0-SNAPSHOT")
    implementation("co.aikar:fastutil-longbase:3.0-SNAPSHOT")
    implementation("co.aikar:fastutil-longhashmap:3.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.7")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.github.retrooper.packetevents:spigot:2.2.0")
    compileOnly("com.ghostchu:quickshop-api:5.2.0.8")
    compileOnly("com.google.guava:guava:33.0.0-jre")
    compileOnly( "net.milkbowl.vault:VaultAPI:1.7")
    compileOnly( "com.sk89q.worldguard:worldguard-bukkit:7.0.9")
    compileOnly( "com.github.TechFortress:GriefPrevention:16.18.1")
    compileOnly( "me.clip:placeholderapi:2.11.5")
    compileOnly( "us.dynmap:dynmap-api:3.4-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    // Cache2k
    val cache2kVersion = "1.2.2.Final"

    implementation("org.cache2k:cache2k-api:${cache2kVersion}")
    runtimeOnly("org.cache2k:cache2k-core:${cache2kVersion}")
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}.jar")

        relocate("co.aikar.commands", "net.crashcraft.crashclaim.acf")
        relocate("co.aikar.idb", "net.crashcraft.crashclaim.idb")
        relocate("co.aikar.taskchain", "net.crashcraft.crashclaim.taskchain")
        relocate("io.papermc.lib", "net.crashcraft.crashclaim.paperlib")
        relocate("org.bstats", "net.crashcraft.crashclaim.bstats")
        relocate("it.unimi.dsi", "net.crashcraft.crashclaim.fastutil")
        relocate("org.cache2k.IntCache", "net.crashcraft.crashclaim.cache2k")
        relocate("com.zaxxer.hikari", "net.crashcraft.crashclaim.hikari")
        relocate("com.github.retrooper.packetevents", "net.crashcraft.crashclaim.packetevents.api")
        relocate("io.github.retrooper.packetevents", "net.crashcraft.crashclaim.packetevents.impl")
    }

    build {
        dependsOn(shadowJar)
        dependsOn(publishToMavenLocal)
    }

    assemble {
        dependsOn(shadowJar)
        dependsOn(publishToMavenLocal)
    }

    compileJava {
        dependsOn(clean)
    }

    processResources {
        expand(project.properties)
    }
}

group = "net.crashcraft"
version = findProperty("version")!!
description = "CrashClaim"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}