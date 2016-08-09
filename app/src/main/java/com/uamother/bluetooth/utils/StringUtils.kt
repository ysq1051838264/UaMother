package com.hdr.wristband.utils

/**
 * Created by hdr on 16/7/4.
 */
object StringUtils {
    fun format(bytes: ByteArray):String {
        val sb = StringBuilder()
        bytes.forEach {
            sb.append(String.format("%02X ", it))
        }
        return sb.toString()
    }
}