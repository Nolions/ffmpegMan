package com.nolions.ffmpegman.view

import Config
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
    private val mediaPath = SimpleStringProperty()
    private val videoFilterList = listOf("*.mp4", "*.avi")
    private val collection = FXCollections.observableArrayList<String>()
    val tmpDir = createTempDirectory("tmp")

    private var dirPath = "."


    private fun convertStringToHex(str: String) {
        val stringBuilder = StringBuilder()
        val charArray = str.toCharArray()
        for (c in charArray) {
            val charToHex = Integer.toHexString(c.toInt())
            stringBuilder.append(charToHex)
        }
        println("Converted Hex from String: $stringBuilder")
    }

    override val root = borderpane() {
        setRunResult(FFMpegUnit("ffmpeg").version().run())

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
     */
    fun videoProcess(filePath: String) {
        val file = FileObj(filePath)
        val path = createDirectory("$tmpDir/${file.name}")
        val m3u8 = "$path/${file.name}.m3u8"

        println(path)
        println(m3u8)
        val result = FFMpegUnit(Config.ffmpeg).convertVodHLS(
            input = mediaPath.value,
            output = m3u8
        ).run()
        setRunResult(result)

        AWSS3Unit.uploadDirectory(file.name, Paths.get(path))
//        AWSS3Unit.putObject(file.name, path)
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
