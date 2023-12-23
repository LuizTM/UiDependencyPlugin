package com.luiztm.ui.dependency.internal.mapper

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
        name = "${value.module}:${value.selectedVersion(this)}".formatWhen(alreadyRendered),
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

private fun String.formatWhen(alreadyRendered: Boolean): String =
    if (alreadyRendered) "(*)$this" else this

private fun ModuleComponentSelector.selectedVersion(original: ResolvedDependencyResult): String {
    return if (this.matchesStrictly(original.selected.id)) {
        this.version
    } else {
        val moduleVersion = (original.selected.id as DefaultModuleComponentIdentifier).version
        if (version.isEmpty()) moduleVersion else "${this.version} -> $moduleVersion"
    }
}
