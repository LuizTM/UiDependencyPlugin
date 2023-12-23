package com.luiztm.ui.dependency.internal.model

import com.luiztm.ui.dependency.internal.style.UiColorSchema

internal data class UiNode(
    val name: String = "",
    val isProject: Boolean = false,
    val alreadyRendered: Boolean = false,
    val style: NodeStyle? = null,
    val artifact: ArtifactModule? = null,
    val children: List<UiNode> = mutableListOf()
)

internal data class ArtifactModule(
    val artifactId: String? = "",
    val group: String? = "",
    val version: String? = ""
) {
    override fun toString(): String = "$group:$artifactId:$version"
}

internal data class NodeStyle(
    val collapsedColor: String = "#FFFFFF",
    val fillColor: String,
    val borderColor: String,
    val linkColor: String,
    val linkTraceColor: String
)

internal fun UiColorSchema.toNodeStyle(isProject: Boolean): NodeStyle {
    return NodeStyle(
        fillColor = if (isProject) projectNodeColor else dependencyNodeColor,
        borderColor = if (isProject) projectNodeColor else dependencyNodeColor,
        linkColor = linkStrokeColor,
        linkTraceColor = linkTraceColor
    )
}
