package com.luiztm.ui.dependency.internal.extensions

import com.luiztm.ui.dependency.internal.extensions.TypeConfiguration.RuntimeClasspath
import com.luiztm.ui.dependency.internal.style.UiColorSchema

/**
 * UI Styles:
 * rootNodeColor
 * projectNodeColor
 * dependencyNodeColor
 * linkNodeColor
 *
 * build.gradle.kts
 *
 * UITreeStyleExtension { style ->
 *    rootNodeColor = "#9F00C5"
 *    projectNodeColor = "#3DDC84"
 *    dependencyNodeColor = "#3DDC84"
 *    linkStrokeColor = "#CCC"
 *    linkStrokePathColor = "#FF4136"
 * }
 *
 */
open class UiTreeStyle(
    override var projectNodeColor: String = "#9F00C5",
    override var dependencyNodeColor: String = "#3DDC84",
    override var linkStrokeColor: String = "#CCC",
    override var linkTraceColor: String = "#FF4136"
) : UiColorSchema

open class UiTreeExtension(
    internal var configuration: TypeConfiguration = RuntimeClasspath(),
    var style: UiColorSchema = UiTreeStyle()
) {
    companion object {
        const val EXTENSION_NAME = "uiTreeExtension"
    }
}

sealed class TypeConfiguration(val type: String, val variant: String = "") {
    class RuntimeClasspath(variant: String = "") : TypeConfiguration("runtimeClassPath", variant)

    // Some problems identify when use it!
    // class CompileClasspath(variant: String = "") : TypeConfiguration("compileClassPath", variant)
}
