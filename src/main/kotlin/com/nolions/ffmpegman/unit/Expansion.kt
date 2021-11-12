package com.nolions.ffmpegman.unit

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.util.*


fun UUID.hex(): String {
    val s = this.toString()
    return s.substring(0, 8) + this.toString().substring(9, 13) + s.substring(14, 18) + s.substring(19, 23) + s.substring(24)
}

fun ByteArray.format() = String.format("%032X", BigInteger(+1, this))

fun ByteArray.hex() = String(this)

fun File.encodeBase64(): String? {
    return try {
        val fileContent = Files.readAllBytes(this.toPath())
        Base64.getEncoder().encodeToString(fileContent)
    } catch (e: IOException) {
        throw IllegalStateException("could not read file $this", e)
    }
}
