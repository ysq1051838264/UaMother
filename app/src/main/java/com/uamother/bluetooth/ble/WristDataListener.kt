package com.hdr.wristband.ble

/**
 * 手环的数据回调
 * Created by ysq on 16/7/31.
 */

data class WristSportData(
        /**
         * 总步数
         */
        val steps: Int,
        /**
         * 总距离
         */
        val distance: Int,
        /**
         * 总卡路里
         */
        val calories: Int


)