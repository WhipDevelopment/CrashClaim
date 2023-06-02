plugins {
    id("java")
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenLocal()
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

    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    mavenCentral()
}

dependencies {
    // Custom Utils
    implementation("com.github.CrashCraftNetwork:CrashUtils:master-SNAPSHOT")
    compileOnly("com.github.Chasewhip8:CrashPayment:master-SNAPSHOT")

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    // Adventure
    val adventure = "4.13.0"
    implementation("net.kyori:adventure-api:${adventure}")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-text-minimessage:${adventure}")

    // Other
    implementation("co.aikar:taskchain-bukkit:3.7.2")
    implementation("net.wesjd:anvilgui:1.6.5-SNAPSHOT")
    implementation("co.aikar:fastutil-base:3.0-SNAPSHOT")
    implementation("co.aikar:fastutil-longbase:3.0-SNAPSHOT")
    implementation("co.aikar:fastutil-longhashmap:3.0-SNAPSHOT")
    implementation("co.aikar:acf-paper:0.5.0-SNAPSHOT")
    implementation("io.papermc:paperlib:1.0.1")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:2.4.1")
    implementation("org.bstats:bstats-bukkit:2.2.1")
    compileOnly( "com.google.guava:guava:29.0-jre")
    //compileOnly( "com.comphenix.protocol:ProtocolLib:4.7.1-SNAPSHOT")
    compileOnly("com.github.dmulloy2:ProtocolLib:5.0.0")
    compileOnly( "net.milkbowl.vault:VaultAPI:1.7")
    compileOnly( "com.sk89q.worldguard:worldguard-bukkit:7.0.5")
    compileOnly( "com.github.TechFortress:GriefPrevention:16.16.0")
    compileOnly( "me.clip:placeholderapi:2.10.10")
    compileOnly( "us.dynmap:dynmap-api:3.2-SNAPSHOT")
    compileOnly("net.luckperms:api:5.4")

    // Cache2k
    val cache2kVersion = "2.4.1.Final"

    implementation("org.cache2k:cache2k-api:${cache2kVersion}")
    runtimeOnly("org.cache2k:cache2k-core:${cache2kVersion}")
}

tasks {
    shadowJar {
        archiveFileName.set("${project.name}-${project.version}.jar")

        relocate("co.aikar.commands", "net.crashcraft.crashclaim.acf")
        relocate("co.aikar.idb", "net.crashcraft.crashclaim.idb")
        relocate("dev.whip.crashutils", "net.crashcraft.crashclaim.crashutils")
        relocate("co.aikar.taskchain", "net.crashcraft.crashclaim.taskchain")
        relocate("io.papermc.lib", "net.crashcraft.crashclaim.paperlib")
        relocate("org.bstats", "net.crashcraft.crashclaim.bstats")
        relocate("it.unimi.dsi", "net.crashcraft.crashclaim.fastutil")
        relocate("org.cache2k.IntCache", "net.crashcraft.crashclaim.cache2k")
        relocate("com.zaxxer.hikari", "net.crashcraft.crashclaim.hikari")
    }

    register<Copy>("buildToServer") {
        from(shadowJar)
        into("./testServer/plugins")
    }

    build {
        dependsOn(shadowJar)
        dependsOn(publishToMavenLocal)
    }

    compileJava {
        options.encoding = "UTF-8"
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
