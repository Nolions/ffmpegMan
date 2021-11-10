package com.nolions.ffmpegman.unit

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


class FFMpegUnit(private val ffmpegExePath: String) {
    private var errorStream: InputStream? = null
    private var inputStreamReader: InputStreamReader? = null
    private var br: BufferedReader? = null
    private var resultCollection = ArrayList<String>()

    private lateinit var media: Media

    private var cmdList = ArrayList<String>()

    init {
        cmdList.add(ffmpegExePath)
    }

    /**
     * FFMpeg 版本資訊
     */
    fun version(): FFMpegUnit {
        cmdList.add("-")
        cmdList.add("version")
        return this
    }

    /**
     * video 轉換成 HLS
     * -----------------------
     * EX: ffmpeg -i <input file> -vcodec copy -acodec copy -hls_time 5 -hls_list_size 0 <output.m3u8>
     */
    fun convertHLS(input: String, code: String = "copy", seconds: Int = 2, size: Int = 0): FFMpegUnit {
        input(input)
        videDecode(code)
        hlsTime(seconds)
        hlsListSize(size)
        output()

        return this
    }

    /**
     * VOD convert HLS
     * -----------------------
     * EX: ffmpeg -i <input file> -vcodec copy -acodec copy -hls_time 5 -hls_list_size 0 -hls_playlist_type vod -hls_flags independent_segments <output.m3u8>
     */
    fun convertVodHLS(
        input: String,
        code: String = "copy",
        seconds: Int = 2,
        size: Int = 0,
        output: String? = null
    ): FFMpegUnit {
        input(input)
        videDecode(code)
        hlsTime(seconds)
        hlsListSize(size)
        playlistType(HlsPlaylistType.VOD)
        hlsFlags(HlsFlagsOperation.INDEPENDENT_SEGMENTS)
        output(output)

        return this
    }

    /**
     * 輸入源
     *
     * @param path String
     * @return FFMpeg
     */
    fun input(path: String): FFMpegUnit {
        cmdList.add("-i")
        cmdList.add(path)

        media = Media(path = path)

        return this
    }

    /**
     * 影片編碼方式
     *
     * @param code String
     * @return FFMpeg
     */
    fun videDecode(code: String = "copy"): FFMpegUnit {
        cmdList.add("-vcodec")
        cmdList.add(code)

        return this
    }

    /**
     * 音訊編碼方式
     *
     * @param code String
     * @return FFMpeg
     */
    fun audioDecode(code: String = "copy"): FFMpegUnit {
        cmdList.add("-acodec")
        cmdList.add(code)

        return this
    }

    /**
     * 切片長度
     *
     * @param seconds Int
     * @return FFMpeg
     */
    fun hlsTime(seconds: Int = 2): FFMpegUnit {
        cmdList.add("-hls_time")
        cmdList.add(seconds.toString())

        return this
    }

    /**
     * playlist播放清單最多的內容，0為無限制，預設為0
     *
     * @param  size Int
     * @return FFMpeg
     */
    fun hlsListSize(size: Int = 0): FFMpegUnit {
        cmdList.add("-hls_list_size")
        cmdList.add(size.toString())

        return this
    }

    /**
     * 輸出檔案
     *
     * @param targeDir String|null
     * @return FFMpeg
     */
    fun output(targetDir: String? = null): FFMpegUnit {
        val outputDir = if (targetDir != null) {
            val theDir = File("$targetDir/${media.name}")
            if (!theDir.exists()) {
                theDir.mkdirs()
            }

            theDir.path
        } else {
            val theDir = File("${media.parent}/${media.name}")
            if (!theDir.exists()) {
                theDir.mkdirs()
            }

            theDir.path
        }

        val meu8File = "$outputDir/${media.name}.m3u8"
        println(meu8File)
        cmdList.add(meu8File)

        return this
    }

    /**
     * 播放列表類型
     * ----------------------
     * hls_playlist_type event 強制hls_list_size為0，且播放列表(playlist)只能附加到
     * hls_playlist_type vod 強制hls_list_size為0，且播放列表不得更改
     * @param type String
     * @return FFMpeg
     */
    // TODO segments => enum
    fun playlistType(type: HlsPlaylistType): FFMpegUnit {
        when (type) {
            HlsPlaylistType.VOD, HlsPlaylistType.EVENT -> {
                cmdList.add("-hls_playlist_type")
                cmdList.add(type.code)
            }
        }

        return this
    }

    /**
     * HLS flag 設定
     * @param segments String
     * @return FFMpeg
     */
    fun hlsFlags(operation: HlsFlagsOperation): FFMpegUnit {
        when (operation) {
            HlsFlagsOperation.SINGLE_FILE,
            HlsFlagsOperation.DELETE_SEGMENTS,
            HlsFlagsOperation.APPEND_LIST,
            HlsFlagsOperation.ROUND_DURATIONS,
            HlsFlagsOperation.DISCONT_START,
            HlsFlagsOperation.OMIT_ENDLIST,
            HlsFlagsOperation.PERIOD_REKEY,
            HlsFlagsOperation.INDEPENDENT_SEGMENTS,
            HlsFlagsOperation.IFRAMES_ONLY,
            HlsFlagsOperation.SPLIT_BY_TIME,
            HlsFlagsOperation.PROGRAM_DATE_TIME,
            HlsFlagsOperation.SECOND_LEVEL_SEGMENT_INDEX,
            HlsFlagsOperation.SECOND_LEVEL_SEGMENT_SIZE,
            HlsFlagsOperation.SECOND_LEVEL_SEGMENT_DURATION
            -> {
                cmdList.add("-hls_flags")
                cmdList.add(operation.code)
            }
        }


        return this
    }

    /**
     * 執行 ffmpeg 指令
     *
     * @return ArrayList<String>
     */
    fun run(): ArrayList<String> {
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
                    resultCollection.add(it.readLine())
//                    println(it.readLine())
//                    println("--------------------------------------------")
                }
            }
        } finally {
            br?.close()
            inputStreamReader?.close()
            errorStream?.close()
            cmdList.clear()
        }

        return resultCollection
    }

    data class Media(val path: String) {
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
}

enum class HlsPlaylistType(val code: String) {
    EVENT("event"),
    VOD("vod"),
}

enum class HlsFlagsOperation(val code: String) {
    SINGLE_FILE("single_file"),
    DELETE_SEGMENTS("delete_segments"),
    APPEND_LIST("append_list"),
    ROUND_DURATIONS("round_durations"),
    DISCONT_START("discont_start"),
    OMIT_ENDLIST("omit_endlist"),
    PERIOD_REKEY("period_rekey"),
    INDEPENDENT_SEGMENTS("independent_segments"),
    IFRAMES_ONLY("iframes_only"),
    SPLIT_BY_TIME("split_by_time"),
    PROGRAM_DATE_TIME("program_date_time"),
    SECOND_LEVEL_SEGMENT_INDEX("second_level_segment_index"),
    SECOND_LEVEL_SEGMENT_SIZE("second_level_segment_size"),
    SECOND_LEVEL_SEGMENT_DURATION("second_level_segment_duration"),
}
