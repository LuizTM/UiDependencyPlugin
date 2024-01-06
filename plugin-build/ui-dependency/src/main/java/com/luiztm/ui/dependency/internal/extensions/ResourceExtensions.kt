package com.luiztm.ui.dependency.internal.extensions

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
