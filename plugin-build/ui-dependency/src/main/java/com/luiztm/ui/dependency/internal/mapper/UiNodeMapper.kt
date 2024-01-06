package com.luiztm.ui.dependency.internal.mapper

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

import com.luiztm.ui.dependency.internal.style.UiColorSchema
import com.luiztm.ui.dependency.internal.model.ArtifactModule
import com.luiztm.ui.dependency.internal.model.UiNode
import com.luiztm.ui.dependency.internal.model.toNodeStyle
import org.gradle.api.artifacts.component.ComponentSelector
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.component.ProjectComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier

internal fun <T : ComponentSelector> ResolvedDependencyResult.mapChildNode(
    data: T,
    extension: UiColorSchema,
    alreadyRendered: Boolean,
    nestedUiNode: (MutableSet<out DependencyResult>) -> List<UiNode>
): UiNode {
    return when (data) {
        is ModuleComponentSelector -> this.resolveModuleType(
            data,
            extension,
            alreadyRendered,
            nestedUiNode
        )
        is ProjectComponentSelector -> this.resolveProjectType(
            data,
            extension,
            nestedUiNode
        )
        else -> UiNode()
    }
}

private fun ResolvedDependencyResult.resolveModuleType(
    value: ModuleComponentSelector,
    extension: UiColorSchema,
    alreadyRendered: Boolean,
    nestedUiNode: (MutableSet<out DependencyResult>) -> List<UiNode>
): UiNode {
    return UiNode(
        name = "${value.module}:${value.selectedVersion(this)}",
        isProject = false,
        artifact = ArtifactModule(
            artifactId = value.module,
            group = value.group,
            version = value.selectedVersion(this)
        ),
        alreadyRendered = alreadyRendered,
        style = extension.toNodeStyle(false),
        children = if (!alreadyRendered) {
            nestedUiNode(selected.dependencies)
        } else { mutableListOf() }
    )
}

private fun ResolvedDependencyResult.resolveProjectType(
    value: ProjectComponentSelector,
    extension: UiColorSchema,
    nestedUiNode: (MutableSet<out DependencyResult>) -> List<UiNode>
): UiNode {
    return UiNode(
        name = value.projectPath,
        isProject = true,
        style = extension.toNodeStyle(true),
        children = nestedUiNode(selected.dependencies)
    )
}

private fun ModuleComponentSelector.selectedVersion(original: ResolvedDependencyResult): String {
    return if (this.matchesStrictly(original.selected.id)) {
        this.version
    } else {
        val moduleVersion = (original.selected.id as DefaultModuleComponentIdentifier).version
        if (version.isEmpty()) moduleVersion else "${this.version} 🔺 $moduleVersion"
    }
}
