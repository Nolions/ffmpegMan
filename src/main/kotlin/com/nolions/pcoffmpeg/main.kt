package com.nolions.pcoffmpeg

import tornadofx.launch

fun main(args: Array<String>) {
    println("ffmpeg Exe: ${args[0]}")
    if (args.isNotEmpty()) {
        Config.ffmpeg = args[0]
    }

    launch<MyApp>()
}
