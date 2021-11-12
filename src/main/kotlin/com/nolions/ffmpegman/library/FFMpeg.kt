package com.nolions.ffmpegman.library

import com.nolions.ffmpegman.unit.format
import com.nolions.ffmpegman.unit.generateKey
import com.nolions.ffmpegman.unit.hex
import java.io.PrintWriter

class FFMpeg(private val ffmpegExePath: String) {
    fun new(): FFMpegCore {
        return FFMpegCore(ffmpegExePath)
    }

    suspend fun genKenInfo(path: String, keyfile: String) {
        val writer = PrintWriter(path)
        writer.println("key://$keyfile")
        writer.println(keyfile)
        writer.println(generateKey()!!.format())
        writer.close()
    }

    suspend fun genKen(path: String) {
        val writer = PrintWriter(path)
        writer.println(generateKey()!!.hex())
        writer.close()
    }
}
