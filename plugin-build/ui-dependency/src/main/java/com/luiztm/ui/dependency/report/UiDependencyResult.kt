package com.luiztm.ui.dependency.report

import com.google.gson.Gson
import com.luiztm.ui.dependency.internal.extensions.UiTreeExtension
import com.luiztm.ui.dependency.internal.mapper.mapChildNode
import com.luiztm.ui.dependency.internal.style.UiColorSchema
import com.luiztm.ui.dependency.internal.model.UiNode
import com.luiztm.ui.dependency.internal.model.UiRootNode
import com.luiztm.ui.dependency.internal.model.toNodeStyle
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult

internal class UiDependencyResult(
    private val target: Project,
    private val extension: UiTreeExtension
) {

    fun provide(): String {
        val result = target
            .filterDependencies(extension)
            .mapTreeNodeDependencies(extension.style)
            .build(target.path, extension.style)

        return Gson().toJson(result)
    }

    private fun Project.filterDependencies(extension: UiTreeExtension): ResolvedComponentResult {
        val configuration = project.configurations
            .filter { it.isCanBeResolved }
            .filter { config ->
                config.name.contains(extension.configuration.type, ignoreCase = true) && listOf(
                    "test",
                    "AndroidTest",
                    "UnitTest"
                ).none {
                    config.name.contains(it)
                }
            }.first { config ->
                config.name.contains(extension.configuration.variant, ignoreCase = true)
            }
        return configuration.incoming.resolutionResult.root
    }

    private fun ResolvedComponentResult.mapTreeNodeDependencies(extension: UiColorSchema): List<UiNode> {
        return dependencies.flatMap { transformToChild(mutableSetOf(it), extension = extension) }
    }

    private fun List<UiNode>.build(projectName: String, extension: UiColorSchema) =
        UiRootNode(
            name = projectName,
            style = extension.toNodeStyle(true),
            children = this
        )

    private fun transformToChild(
        dep: MutableSet<out DependencyResult>,
        visited: MutableSet<ComponentIdentifier> = mutableSetOf(),
        extension: UiColorSchema
    ): List<UiNode> {
        return dep.filterIsInstance<ResolvedDependencyResult>().map {
            val alreadyVisited = !visited.add(it.selected.id)
            val alreadyRendered = it.selected.dependencies.isNotEmpty() && alreadyVisited
            it.mapChildNode(it.requested, extension, alreadyRendered) { selectedDep ->
                transformToChild(selectedDep, visited, extension)
            }
        }
    }
}
