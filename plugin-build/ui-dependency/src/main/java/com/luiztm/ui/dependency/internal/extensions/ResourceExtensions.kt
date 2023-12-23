package com.luiztm.ui.dependency.internal.extensions

import java.io.InputStream
import java.net.URL

internal fun URL.openSafeStream(): InputStream {
    return openConnection().apply { useCaches = false }.getInputStream()
}

internal fun <T> Class<T>.getSafeResourceAsStream(name: String): InputStream? {
    return classLoader.getResource(name)?.openSafeStream()
}

/**
 * Get the given file content as string.
 */
internal fun Any.getTextResourceContent(fileName: String): String {
    return javaClass.classLoader.getResource("$fileName")!!
        .openSafeStream()
        .bufferedReader()
        .use { it.readText() }
}
