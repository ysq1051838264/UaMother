package com.hdr.wristband.model

/**
 * Created by hdr on 16/7/4.
 */
data class LogModel(

        val content: String,
        val time: Long = System.currentTimeMillis()
)