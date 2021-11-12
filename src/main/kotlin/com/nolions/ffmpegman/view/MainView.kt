package com.nolions.ffmpegman.view

import com.nolions.ffmpegman.library.FFMpeg
import com.nolions.ffmpegman.model.FileObj
import com.nolions.ffmpegman.unit.AWSS3Unit
import com.nolions.ffmpegman.unit.createDirectory
import com.nolions.ffmpegman.unit.createTempDirectory
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.stage.FileChooser
import kotlinx.coroutines.*
import tornadofx.*
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainView : View("Hello TornadoFX") {
    private lateinit var threadDispatcher: ExecutorCoroutineDispatcher

    private val ffMpeg = FFMpeg("ffmpeg")
    private val mediaPath = SimpleStringProperty()
    private val videoFilterList = listOf("*.mp4", "*.avi")
    private val collection = FXCollections.observableArrayList<String>()

    private var dirPath = "."

    override val root = borderpane() {
        setCoroutinePool()

//        setRunResult(ffMpeg.new().version().build().result())

        center = hbox {
            textfield(mediaPath) {
            }

            button("Chose") {
                action {
                    chooseVideo()
                }
            }

            button("Convert") {
                action {
                    videoProcess(mediaPath.value)

                }
            }
        }

        bottom = listview(collection)
    }

    /**
     * create temp dir
     * convert to HLS
     * update to S3
     * remove temp dir
     */
    private fun videoProcess(filePath: String) {
        val file = FileObj(filePath)

        val tmpDir = createTempDirectory(file.uid)

        val s3Prefix = "sglive/hls"
        val hlsPath = "$tmpDir/hls"

        val m3u8 = "$hlsPath/video.m3u8"
        val keyInfo = "${Paths.get(tmpDir)}/${file.uid}.keyinfo"
        val key = "${Paths.get(tmpDir)}/${file.uid}.key"

        println(m3u8)
        println(keyInfo)
        println(key)

        CoroutineScope(Dispatchers.IO).launch {
            createDirectory(hlsPath)

            ffMpeg.genKen(key)
            ffMpeg.genKenInfo(keyInfo, key)
            val result = ffMpeg.new().convertHLS(
                input = mediaPath.value,
                output = m3u8,
                vod = true,
//                encryptKey = File(keyInfo)
            ).build().result()


            AWSS3Unit.uploadDirectory("$s3Prefix/${file.uid}", Paths.get(tmpDir))

            withContext(threadDispatcher) {
                setRunResult(result)
            }
        }

    }


    private fun setCoroutinePool() {
        val threadPool = Executors.newSingleThreadExecutor()
        threadDispatcher = threadPool.asCoroutineDispatcher()
    }

    private fun setRunResult(result: ArrayList<String>) {
        collection.add("=========================")
        collection.addAll(result)
    }

    private fun chooseVideo() {
        val fileChooser = chooserFiles(videoFilterList)

        if (fileChooser.isNotEmpty()) {
            val imgPath = fileChooser.first().toString().replace("\\", "/")
            mediaPath.value = imgPath
            log.info("file: $imgPath")
        }
    }

    private fun chooserFiles(
        extensions: List<String>,
        title: String = "Chose Image",
        mode: FileChooserMode = FileChooserMode.Single
    ): List<File> {
        val filters = arrayOf(FileChooser.ExtensionFilter("$extensions", extensions))

        return chooseFile(
            title = title,
            filters = filters,
            mode = mode
        ) {
            initialDirectory = File(dirPath)
        }
    }
}
