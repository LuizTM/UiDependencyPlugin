package com.luiztm.ui.dependency.report

/**
Copyright (C) 2024 LuizTM

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import com.luiztm.ui.dependency.internal.extensions.getSafeResourceAsStream
import com.luiztm.ui.dependency.internal.extensions.getTextResourceContent
import com.luiztm.ui.dependency.internal.extensions.printOutputMessage
import java.io.File
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class UiDependencyReportTask @Inject constructor(
    private val project: Project
) : DefaultTask() {

    init {
        description = "Generate a simple UI tree to display your Gradle dependencies"
        group = "UI Dependencies Tree"
    }

    private val rawJS: String by lazy { getTextResourceContent("uiTreeView-template.js") }
    private val rawHTML: String by lazy { getTextResourceContent("index-template.html") }
    private val savePath = project.layout.buildDirectory.dir("ui-dependencies-plugin").get()

    private val filesPath: List<String> = listOf(
        "res/d3.v5.min.js",
        "res/jquery-3.3.1.min.js",
        "res/select2.min.js",
        "res/css/bootstrap.min.css",
        "res/bootstrap.bundle.min.js",
        "res/css/select2.min.css",
        "index-template.html",
        "uiTreeView-template.js"
    )

    @get:Input
    abstract val data: Property<String>

    @TaskAction
    fun process() {
        filesPath.forEach {
            FileUtils.copyInputStreamToFile(
                javaClass.getSafeResourceAsStream(it),
                File("$savePath/${it.replace("-template", "-${project.name}")}")
            )
        }

        File("$savePath/uiTreeView-${project.name}.js")
            .writeText(rawJS.replace("%json_here%", data.get()))

        val pathIndexFile = File("$savePath/index-${project.name}.html").apply {
            writeText(rawHTML.replace("%file-js%", "-${project.name}"))
        }

        logger.printOutputMessage(pathIndexFile)
    }

    companion object {
        const val TASK_NAME = "showUiDependencies"
    }
}
