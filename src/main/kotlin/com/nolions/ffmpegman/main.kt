package com.nolions.ffmpegman

import Config
import com.nolions.ffmpegman.unit.AWSS3Unit
import tornadofx.*

fun main(args: Array<String>) {
    println("ffmpeg Exe: ${args[0]}")
    var ffmpegExe: String? = null
    var awsID: String? = null
    var awsKey: String? = null
    var awsBucket: String? = null

    if (args.size < 3) {
        // TODO ERROR
        println("缺少必要參數!!")
    } else if (args.size == 3) {
        ffmpegExe = args[0]
        awsID = args[1]
        awsKey = args[2]
    } else {
        ffmpegExe = args[0]
        awsID = args[1]
        awsKey = args[2]
        awsBucket = args[3]
    }

    Config.ffmpeg = ffmpegExe!!
    AWSS3Unit.init(awsID!!, awsKey!!, awsBucket)
    println(awsKey)


    launch<MyApp>()
}
