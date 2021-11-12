package com.nolions.ffmpegman.view

import com.nolions.ffmpegman.library.FFMpeg
import com.nolions.ffmpegman.model.FileObj
import com.nolions.ffmpegman.unit.*
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File
import java.nio.file.Paths
import java.util.*

class MainView : View("Hello TornadoFX") {
    val ffMpeg = FFMpeg("ffmpeg")
    private val mediaPath = SimpleStringProperty()
    private val videoFilterList = listOf("*.mp4", "*.avi")
    private val collection = FXCollections.observableArrayList<String>()
    val tmpDir = createTempDirectory("tmp")

    private var dirPath = "."

    override val root = borderpane() {
        setRunResult(ffMpeg.new().version().build().result())

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
        val s3Prefix = "sglive/hls"
        val file = FileObj(filePath)
        val path = createDirectory("$tmpDir/${file.uid}")
        val m3u8 = "$path/video.m3u8"

        val result = ffMpeg.new().convertVodHLS(
            input = mediaPath.value,
            output = m3u8
        ).build().result()
        setRunResult(result)

        AWSS3Unit.uploadDirectory("$s3Prefix/${file.uid}", Paths.get(path))
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
