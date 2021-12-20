version = "1.0.0"

project.extra["PluginName"] = "rChickenSlayer"
project.extra["PluginDescription"] = "Kills chickens"

dependencies {
    compileOnly(project(":iutils"))
}

tasks {
    jar {
        manifest {
            attributes(mapOf(
                    "Plugin-Version" to project.version,
                    "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                    "Plugin-Provider" to "Ryder",
                    "Plugin-Dependencies" to nameToId("iUtils"),
                    "Plugin-Description" to project.extra["PluginDescription"],
                    "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}