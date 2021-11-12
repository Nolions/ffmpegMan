package com.nolions.ffmpegman.library

import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader

class FFMpegCore(ffmpegExePath: String) {
    private var errorStream: InputStream? = null
    private var inputStreamReader: InputStreamReader? = null
    private var br: BufferedReader? = null

    private var commends = ArrayList<String>()
    private lateinit var resultCollection: ArrayList<String>

    init {
        commends.add(ffmpegExePath)
    }

    /**
     * FFMpeg 版本資訊
     */
    fun version(): FFMpegCore {
        commends.add("-")
        commends.add("version")
        return this
    }

    /**
     * 取得影片長度
     *
     * @param input String
     */
    fun mediaInfo(input: String): String {
        input(input)
        resultCollection.forEach { it ->
            val str = it.trim()
            if (str.startsWith("Duration:")) {
                return str.substring(0, str.indexOf(","))
            }
        }

        return "--:--:--"
    }

    /**
     * video 轉換成 HLS
     * -----------------------
     * EX:
     *  ffmpeg -i <input file> -vcodec copy -acodec copy -hls_time 5 -hls_list_size 0 <output.m3u8>
     *  ffmpeg -i <input file> -vcodec copy -acodec copy -hls_time 5 -hls_list_size 0 -hls_playlist_type vod -hls_flags independent_segments <output.m3u8>
     */
    fun convertHLS(
        input: String,
        code: String = "copy",
        seconds: Int = 2,
        size: Int = 0,
        output: String,
        encryptKey: File? = null,
        vod: Boolean = false
    ): FFMpegCore {
        input(input)
        videDecode(code)
        hlsTime(seconds)
        hlsListSize(size)
        if (vod) {
            playlistType(HlsPlaylistType.VOD)
            hlsFlags(HlsFlagsOperation.INDEPENDENT_SEGMENTS)
        }
        if (encryptKey != null) {
            hlsEncrypt(encryptKey)
        }

        output(output)

        return this
    }

    /**
     * 輸入源
     *
     * @param path String
     * @return FFMpeg
     */
    fun input(path: String): FFMpegCore {
        commends.add("-i")
        commends.add(path)

        return this
    }

    /**
     * 影片編碼方式
     *
     * @param code String
     * @return FFMpeg
     */
    fun videDecode(code: String = "copy"): FFMpegCore {
        commends.add("-vcodec")
        commends.add(code)

        return this
    }

    /**
     * 音訊編碼方式
     *
     * @param code String
     * @return FFMpeg
     */
    fun audioDecode(code: String = "copy"): FFMpegCore {
        commends.add("-acodec")
        commends.add(code)

        return this
    }

    /**
     * 切片長度
     *
     * @param seconds Int
     * @return FFMpeg
     */
    fun hlsTime(seconds: Int = 2): FFMpegCore {
        commends.add("-hls_time")
        commends.add(seconds.toString())

        return this
    }

    /**
     * playlist播放清單最多的內容，0為無限制，預設為0
     *
     * @param  size Int
     * @return FFMpeg
     */
    fun hlsListSize(size: Int = 0): FFMpegCore {
        commends.add("-hls_list_size")
        commends.add(size.toString())

        return this
    }

    fun hlsEncrypt(keyInfoFile: File): FFMpegCore {
        commends.add("-hls_key_info_file")
        commends.add(keyInfoFile.path)

        return this
    }

    var output: String? = null

    /**
     * 輸出檔案
     *
     * @param targetDir String|null
     * @return FFMpeg
     */
    fun output(target: String): FFMpegCore {
        commends.add(target)

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
    fun playlistType(type: HlsPlaylistType): FFMpegCore {
        when (type) {
            HlsPlaylistType.VOD, HlsPlaylistType.EVENT -> {
                commends.add("-hls_playlist_type")
                commends.add(type.code)
            }
        }

        return this
    }

    /**
     * HLS flag 設定
     * @param segments String
     * @return FFMpeg
     */
    fun hlsFlags(operation: HlsFlagsOperation): FFMpegCore {
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
                commends.add("-hls_flags")
                commends.add(operation.code)
            }
        }


        return this
    }

    /**
     * 執行 ffmpeg 指令
     *
     * @return ArrayList<String>
     */
    suspend fun build(): FFMpegCore {
        resultCollection = ArrayList()

        val builder = ProcessBuilder(commends)
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
                    val str = it.readLine().toString()
                    resultCollection.add(str)
//                    println(str)
                }
            }
        } catch (e: Exception) {

        } finally {
            br?.close()
            inputStreamReader?.close()
            errorStream?.close()
            commends.clear()
        }

        return this
    }

    fun result(): ArrayList<String> {
        return resultCollection
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
