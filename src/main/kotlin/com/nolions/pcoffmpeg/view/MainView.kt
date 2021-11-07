package com.nolions.pcoffmpeg.view

import com.nolions.pcoffmpeg.library.FFMpeg
import com.nolions.pcoffmpeg.library.MediaFile
import javafx.collections.FXCollections
import tornadofx.*

class MainView : View("Hello TornadoFX") {
    private val cmd = ArrayList<String>()

    override val root = borderpane() {
        val collection = FXCollections.observableArrayList<String>()
        val ffmpeg = FFMpeg("ffmpeg")
        ffmpeg.version(collection)

        center = vbox {
            button("GO") {
                action {
                    val collection = FXCollections.observableArrayList<String>()
                    collection.clear()
                    ffmpeg.convertHLS(
                        file = MediaFile("/Users/nolions/Downloads/demo/demo.mp4"),
                        collection = collection
                    )
                }
            }
        }

        bottom = listview(collection)
    }
}
