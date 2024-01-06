package com.luiztm.ui.dependency.internal.model

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
