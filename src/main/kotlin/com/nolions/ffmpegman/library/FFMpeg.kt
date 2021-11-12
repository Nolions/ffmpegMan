package com.nolions.ffmpegman.library

class FFMpeg(private val ffmpegExePath: String) {
    fun new(): FFMpegCore {
        return FFMpegCore(ffmpegExePath)
    }
}