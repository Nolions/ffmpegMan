package com.nolions.ffmpegman.unit

import java.io.File
import java.nio.file.Files
import java.util.*

fun createTempDirectory(dirName: String): String = Files.createTempDirectory(dirName).toFile().absolutePath

fun createDirectory(path: String): String {
    val theDir = File(path)
    if (!theDir.exists()) {
        theDir.mkdirs()
    }

    return path
}
