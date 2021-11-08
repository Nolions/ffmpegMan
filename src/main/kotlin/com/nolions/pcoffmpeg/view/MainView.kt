package com.nolions.pcoffmpeg.view

import com.nolions.pcoffmpeg.library.FFMpeg
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class MainView : View("Hello TornadoFX") {
    private val cmd = ArrayList<String>()
    private val mediaPath = SimpleStringProperty()
    private val videoFilterList = listOf("*.mp4", "*.avi")
    private val collection = FXCollections.observableArrayList<String>()

    private var dirPath = "."

    override val root = borderpane() {
        setRunResult(FFMpeg("ffmpeg").version().run())

        center = vbox {
            hbox {
                textfield(mediaPath) {
                }

                button("Chose") {
                    action {
                        chooseVideo()
                    }
                }
            }

            button("Convert") {
                action {
                    val result = FFMpeg(Config.ffmpeg).convertVodHLS(mediaPath.value).run()
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
