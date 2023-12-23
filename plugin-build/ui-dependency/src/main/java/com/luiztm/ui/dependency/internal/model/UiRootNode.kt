package com.luiztm.ui.dependency.internal.model

internal data class UiRootNode(
    val name: String = "",
    val style: NodeStyle? = null,
    val children: List<UiNode> = emptyList()
)
