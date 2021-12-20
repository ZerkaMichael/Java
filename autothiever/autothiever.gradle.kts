version = "1.0.3"

project.extra["PluginName"] = "Auto Thiever"
project.extra["PluginDescription"] = "Automatically thieves from npcs"

dependencies {
    compileOnly(project(":iutils"))
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to project.extra["PluginProvider"],
                    "Plugin-Dependencies" to nameToId("iUtils"),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}