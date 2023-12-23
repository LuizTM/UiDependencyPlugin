package com.luiztm.ui.dependency.report

import com.luiztm.ui.dependency.internal.extensions.getSafeResourceAsStream
import com.luiztm.ui.dependency.internal.extensions.getTextResourceContent
import java.io.File
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class UiDependencyReportTask @Inject constructor(private val project: Project) : DefaultTask() {

    init {
        description = "Just a sample template task"

        // Don't forget to set the group here.
        // group = BasePlugin.BUILD_GROUP
    }

    private val rawJS: String = getTextResourceContent("dndTree-template.js")
    private val rawHTML: String = getTextResourceContent("index-template.html")
    private val savePath = project.layout.buildDirectory.dir("tree-deps").get()

    private val filesPath: List<String> = listOf(
        "res/d3.v5.min.js",
        "res/jquery-3.3.1.min.js",
        "res/select2.min.js",
        "res/css/bootstrap.min.css",
        "res/bootstrap.bundle.min.js",
        "res/css/select2.min.css",
        "index-template.html",
        "dndTree-template.js"
    )

    @get:Input
    abstract val data: Property<String>
//
//    @get:OutputFiles
//    val outputFiles: ConfigurableFileCollection =
//        project.objects.fileCollection().from(
//            project.layout.buildDirectory.file("dndTree-${project.name}.js"),
//            project.layout.buildDirectory.file("index-${project.name}.html")
//        )

    @TaskAction
    fun process() {
        filesPath.forEach {
            FileUtils.copyInputStreamToFile(
                javaClass.getSafeResourceAsStream(it),
                File("$savePath/${it.replace("-template", "-${project.name}")}")
            )
        }

        File("$savePath/dndTree-${project.name}.js")
            .writeText(rawJS.replace("%json_here%", data.get()))

        File("$savePath/index-${project.name}.html")
            .writeText(rawHTML.replace("%file-js%", "-${project.name}"))
    }

    companion object {
        const val TASK_NAME = "uiTreeDependency"
    }
}
