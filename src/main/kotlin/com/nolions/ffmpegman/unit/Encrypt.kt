package com.nolions.ffmpegman.unit

import javax.crypto.KeyGenerator

fun generateKey(): ByteArray? {
    val gen = KeyGenerator.getInstance("AES")
    gen.init(128) /* 128-bit AES */
    val secret = gen.generateKey()

    return secret.encoded
}
