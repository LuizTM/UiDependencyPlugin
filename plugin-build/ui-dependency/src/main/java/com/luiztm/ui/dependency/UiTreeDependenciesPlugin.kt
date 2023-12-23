package com.luiztm.ui.dependency

import com.luiztm.ui.dependency.internal.extensions.UiTreeExtension
import com.luiztm.ui.dependency.report.UiDependencyReportTask
import com.luiztm.ui.dependency.report.UiDependencyResult
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

open class UiTreeDependenciesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create<UiTreeExtension>(UiTreeExtension.EXTENSION_NAME)
        val dependencyResult = UiDependencyResult(target, extension)

        target.tasks.register(
            UiDependencyReportTask.TASK_NAME,
            UiDependencyReportTask::class.java
        ) {
            this.data.set(dependencyResult.provide())
        }
    }
}
