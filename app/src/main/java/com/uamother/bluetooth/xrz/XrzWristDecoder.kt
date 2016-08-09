package com.hdr.wristband.xrz

import android.util.Log
import com.hdr.wristband.ble.CommandSender
import com.hdr.wristband.ble.WristDecoder
import com.hdr.wristband.ble.WristSportData
import com.hdr.wristband.utils.StringUtils
import com.uamother.bluetooth.xrz.ProtocolHelper
import rx.Observable
import java.util.*

/**
 * Created by ysq on 16/7/31.
 */
class XrzWristDecoder(commandSender: CommandSender) : WristDecoder(commandSender) {


    var data: ByteArray? = null
    var dataLength = -1


    override fun onReceiveData(uuid: UUID, pkgData: ByteArray) {

        //        val code = StringUtils.format(pkgData)
//        Log.i("hdr", "收到数据:$code")
//        //先判断包的完整性,如果不完整,则接上包,并且等待下一数据
//        if (data == null) {
//            //这是一个新包
//            data = pkgData
////            dataLength = pkgData[2].convertInt(pkgData[3])
//        } else {
//            data = data!!.plus(pkgData)
//        }
//
//        val status = String.format("%02X ", pkgData[2])
//
//        if (status.equals("B0")) {
//            if (String.format("%02X ", pkgData[3]).equals("C5")) {
//                //保存到数据库，用来判断是不是一个私人定制模式
//            }
//
//        } else if (status.equals("B1")) {
//            //存储数据
//        }

        val code = StringUtils.format(pkgData)
        Log.i("ysq", "收到数据:$code")
        //先判断包的完整性,如果不完整,则接上包,并且等待下一数据
        if (data == null) {
            //这是一个新包
            data = pkgData
            dataLength = pkgData[2].convertInt(pkgData[3])
        } else {
            data = data!!.plus(pkgData)
        }
        val length = dataLength
        data?.let {
            data ->
            if (data.size != length) {
                Log.i("ysq", "这个数据包还不完整")
                return
            } else {
                this.data = null
                dataLength = -1
            }
            if (data.size > 11) {
                //说明指定中有数据段,先对数据段进行解密
                ProtocolHelper.decryptData(data)
            }
            val code = StringUtils.format(data)
            Log.i("ysq", "解密数据:$code")
            when (data[5].convertInt()) {
                0x05 -> {
                    //这个是获取电量的
                    subscribers[cmd_get_save_value]?.send(data[9].toInt())
                }
                0x16 -> {
                    Log.i("ysq", "用户又触摸了屏幕")
                }
                0x0A -> {
                    val sportData = WristSportData(
                            steps = data[9].convertInt(data[10]),
                            distance = data[11].convertInt(data[12]),
                            calories = data[13].convertInt(data[14])
                    )
                    subscribers[cmd_get_sport_data]?.send(sportData)
                }
                else -> {

                }
            }
        }


    }


    override fun getSaveValue(): Observable<Unit> = observe(cmd_get_save_value) {
        val cmd = ProtocolHelper.merge();
        commandSender.send(cmd)
    }

    override fun getSportData(dayIndex: Int): Observable<WristSportData> = observe(cmd_get_sport_data) {
        val cmd = ProtocolHelper.merge(byteArrayOf(0xC7.toByte(), 0x0A.toByte()), byteArrayOf(dayIndex.toByte()));
        commandSender.send(cmd)
    }

    override fun writeData(): Observable<Unit> = observe(cmd_write_data) {
        //00是保存
        val cmd = ProtocolHelper.mergeData(byteArrayOf(0x76.toByte(), 0xa9.toByte(),0xac.toByte()), 0x00);
        commandSender.send(cmd)
    }

    fun Byte.convertInt(): Int = (this + 256) % 256

    fun Byte.convertInt(b1: Byte): Int = this.convertInt() * 256 + b1.convertInt()
}