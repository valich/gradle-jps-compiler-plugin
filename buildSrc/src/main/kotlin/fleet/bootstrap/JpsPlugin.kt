package fleet.bootstrap

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.task
import java.io.File

class JpsPlugin : Plugin<Project> {
    companion object {
        const val KOTLIN_PLUGIN_DIR = "kotlin"
        const val DEFAULT_KOTLIN_PLUGIN_VERSION = "1.3.72-release-IJ2020.1-6:ideadev"

        const val JPS_DIR = "jps"
        const val JPS_VERSION = "201.8538.31"
    }

    override fun apply(project: Project) {
        // TODO configurable

        createJpsConfiguration(project)

        val pluginVersion = DEFAULT_KOTLIN_PLUGIN_VERSION
        val groupId =
                if (pluginVersion.indexOf(":") == -1) "com.jetbrains.plugins"
                else "${pluginVersion.substringAfter(":")}.com.jetbrains.plugins"
        val pluginArtifactVersion = pluginVersion.substringBefore(":")

        with(project) {
            val kotlinPlugin = configurations.create("kotlinPlugin")

            repositories {
                maven(url = "https://cache-redirector.jetbrains.com/plugins.jetbrains.com/maven")
            }

            dependencies {
                kotlinPlugin("$groupId:org.jetbrains.kotlin:$pluginArtifactVersion@zip")
            }

            task<Sync>("setupKotlinPlugin") {
                from(zipTree(kotlinPlugin.singleFile))
                into("${project.buildDir}/$KOTLIN_PLUGIN_DIR")
            }

            task("setupJpsDeps") {
                val jpsDir = "$buildDir/$JPS_DIR"
                outputs.dir(jpsDir)
                doFirst {
                    val archive = "$buildDir/jps-deps.zip"
                    ant.invokeMethod("get", mapOf("src" to
                            "https://www.jetbrains.com/intellij-repository/releases/com/jetbrains/intellij/idea/jps-standalone/$JPS_VERSION/jps-standalone-$JPS_VERSION.zip",
                            "dest" to archive))
                    copy {
                        from(zipTree(archive))
                        into(jpsDir)
                    }
                    File(archive).delete()
                }
            }
        }
    }
}