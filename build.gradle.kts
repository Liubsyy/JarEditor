
plugins {
    // Java support
    id("java")
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij") version "0.7.3"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project
val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project
val sourceVersion: String by project
val targetVersion: String by project

group = pluginGroup

fun readPluginVersion(): String {
    val pluginXml = file("src/main/resources/META-INF/plugin.xml")

    require(pluginXml.exists()) {
        "plugin.xml not found at ${pluginXml.absolutePath}"
    }
    val document = javax.xml.parsers.DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder()
            .parse(pluginXml)

    document.documentElement.normalize()
    val versionNode = document.getElementsByTagName("version").item(0)
            ?: error("<version> tag not found in plugin.xml")

    return versionNode.textContent.trim()
}
var plugin_version = readPluginVersion()


// Configure project's dependencies
repositories {
    mavenCentral()
//    jcenter()
}
dependencies {
    // https://mvnrepository.com/artifact/org.javassist/javassist
    implementation("org.javassist:javassist:3.30.2-GA")

    // https://mvnrepository.com/artifact/org.ow2.asm/asm
    implementation("org.ow2.asm:asm:9.7")

    // https://mvnrepository.com/artifact/org.ow2.asm/asm-commons
    implementation("org.ow2.asm:asm-commons:9.7")

    // https://mvnrepository.com/artifact/org.benf/cfr
    implementation("org.benf:cfr:0.152")

    // https://mvnrepository.com/artifact/org.bitbucket.mstrobel/procyon-compilertools
    implementation("org.bitbucket.mstrobel:procyon-compilertools:0.6.0")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.8.9")

    //https://mvnrepository.com/artifact/io.github.skylot/jadx-core
    implementation("io.github.skylot:jadx-core:1.5.3")
    //https://mvnrepository.com/artifact/io.github.skylot/jadx-java-input
    implementation("io.github.skylot:jadx-java-input:1.5.3")
}


intellij {
    pluginName = pluginName_
    version = platformVersion
    type = platformType
    downloadSources = platformDownloadSources.toBoolean()
    updateSinceUntilBuild = false

    // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file.
    setPlugins(*platformPlugins.split(',').map(String::trim).filter(String::isNotEmpty).toTypedArray())
}


tasks {
    withType<JavaCompile> {
        sourceCompatibility = sourceVersion
        targetCompatibility = targetVersion
        options.encoding = "UTF-8"
    }

    patchPluginXml {
        sinceBuild(pluginSinceBuild)
        untilBuild(pluginUntilBuild)
    }

    buildSearchableOptions{
        enabled = false
    }

    publishPlugin {
        token(System.getenv("PUBLISH_TOKEN"))
    }

    buildPlugin {
        archiveBaseName.set(project.name)
        archiveVersion.set(plugin_version)
    }
}
