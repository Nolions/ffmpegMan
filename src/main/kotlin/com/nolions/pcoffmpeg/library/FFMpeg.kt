package com.nolions.pcoffmpeg.library

import javafx.collections.ObservableList
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class FFMpeg(private val ffmpegExePath: String) {
    private val ffmpegCmd = ArrayList<String>()
    private var errorStream: InputStream? = null
    private var inputStreamReader: InputStreamReader? = null
    private var br: BufferedReader? = null

    init {
        new()
    }

    private fun new() {
        ffmpegCmd.add(ffmpegExePath)
    }

    private fun run(cmds: ArrayList<String>, collection: ObservableList<String>) {
        val cmdList = ffmpegCmd + cmds
        val builder = ProcessBuilder(cmdList)
        val process = builder.start()
        errorStream = process.errorStream

        errorStream?.let {
            inputStreamReader = InputStreamReader(it)
        }

        inputStreamReader.let {
            br = BufferedReader(it)
        }

        try {
            br?.let {
                while (it.readLine() != null) {
                    collection.add(it.readLine())
                    println(it.readLine())
                    println("----------------------")
                }
            }
        } finally {
            br?.close()
            inputStreamReader?.close()
            errorStream?.close()
            cmds.clear()
        }
    }

    /**
     * ffmpeg version
     */
    fun version(collection: ObservableList<String>) {
        val cmds = ArrayList<String>()
        cmds.add("-")
        cmds.add("version")

        run(cmds, collection)
    }

    /**
     * media convert to HLS
     * -----------------------
     * ffmpeg -i <input file> -vcodec copy -acodec copy -hls_time 60 -hls_list_size 0 <output.m3u8>
     */
    fun convertHLS(
        file: MediaFile,
        time: Int = 60,
        listSize: Int = 0,
        collection: ObservableList<String>
    ) {
        val hlsFile = "${file.parent}/${file.name}.m3u8"

        val cmds = ArrayList<String>()
        cmds.add("-i")
        cmds.add(file.path)

        cmds.add("-vcodec")
        cmds.add("copy")

//        cmds.add("-acodec")
//        cmds.add("copy")

        cmds.add("-hls_time")
        cmds.add(time.toString())

        cmds.add("-hls_list_size")
        cmds.add(listSize.toString())

        cmds.add(hlsFile)
        run(cmds, collection)
    }
}

data class MediaFile(val path: String) {
    private val _file = File(path)
    val file = _file
    val parent: String = _file.parent
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