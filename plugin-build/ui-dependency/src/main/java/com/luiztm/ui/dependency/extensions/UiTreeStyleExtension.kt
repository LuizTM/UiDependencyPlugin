package com.luiztm.ui.dependency.extensions

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

import com.luiztm.ui.dependency.extensions.TypeConfiguration.RuntimeClasspath
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
    val style: UiColorSchema = UiTreeStyle(),
    var constraints: ((group: String?, artifact: String?, version: String?) -> Boolean)? = null
) {
    companion object {
        const val EXTENSION_NAME = "uiTreeExtension"
    }
}

sealed class TypeConfiguration(val type: String, val variant: String = "") {
    class RuntimeClasspath(variant: String = "") : TypeConfiguration("debugRuntimeClasspath", variant)

    // Some problems identify when use it!
    // class CompileClasspath(variant: String = "") : TypeConfiguration("compileClassPath", variant)
}
