package com.luiztm.ui.dependency.internal

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

import com.google.gson.Gson
import com.luiztm.ui.dependency.extensions.UiTreeExtension
import com.luiztm.ui.dependency.internal.mapper.mapChildNode
import com.luiztm.ui.dependency.internal.model.UiNode
import com.luiztm.ui.dependency.internal.model.UiRootNode
import com.luiztm.ui.dependency.internal.model.toNodeStyle
import com.luiztm.ui.dependency.internal.style.UiColorSchema
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
