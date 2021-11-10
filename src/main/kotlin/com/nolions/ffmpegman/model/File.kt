package com.nolions.ffmpegman.model

import java.io.File
import java.util.*

data class FileObj(val path: String) {
    private val uid = UUID.randomUUID().version()
    private val _file = File(path)
    val file = _file
    val name: String
        get() {
            var fname = _file.name
            val pos = fname.lastIndexOf(".")
            if (pos > 0) {
                fname = fname.substring(0, pos)
            }
            return fname
        }
    val extension: String
        get() {
            var ext = ""
            val index: Int = path.lastIndexOf('.')
            if (index > 0) {
                ext = path.substring(index + 1)
            }
            return ext
        }
}

