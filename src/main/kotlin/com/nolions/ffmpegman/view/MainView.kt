package com.nolions.ffmpegman.view

import Config
import com.nolions.ffmpegman.unit.FFMpeg
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.stage.FileChooser
import tornadofx.*
import java.io.*
import java.nio.file.Files


class MainView : View("Hello TornadoFX") {
    private val mediaPath = SimpleStringProperty()
    private val videoFilterList = listOf("*.mp4", "*.avi")
    private val collection = FXCollections.observableArrayList<String>()
    val tempDir = Files.createTempDirectory("tmp").toFile().absolutePath

    private var dirPath = "."

    private fun newOutputStream(fileName: String, charsetName: String = "utf-8"): Writer {
        return BufferedWriter(OutputStreamWriter(FileOutputStream(fileName), charsetName))
    }

    override val root = borderpane() {

        setRunResult(FFMpeg("ffmpeg").version().run())

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
                    val result = FFMpeg(Config.ffmpeg).convertVodHLS(
                        input = mediaPath.value,
                        output = tempDir
                    ).run()
                    setRunResult(result)
                }
            }
        }

        bottom = listview(collection)
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
