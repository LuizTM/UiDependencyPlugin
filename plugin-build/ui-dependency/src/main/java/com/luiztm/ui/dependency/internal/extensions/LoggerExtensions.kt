package com.luiztm.ui.dependency.internal.extensions

import java.io.File
import org.gradle.api.logging.Logger
import org.gradle.internal.logging.ConsoleRenderer

internal fun Logger.printOutputMessage(outputFile: File) {
    lifecycle(
        "See the UI Dependencies report at: \n{}",
        buildString {
            val divider = StringBuilder().apply {
                repeat(outputFile.path.length + 11) {
                    append("=")
                }
            }
            appendLine("$divider ")
            appendLine("| ${ConsoleRenderer().asClickableFileUrl(outputFile)} |")
            appendLine("$divider ")
        }.trimIndent()
    )
}
