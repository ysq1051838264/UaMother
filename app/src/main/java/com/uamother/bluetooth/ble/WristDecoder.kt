package com.hdr.wristband.ble

import android.util.Log
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import java.util.*
import java.util.concurrent.TimeUnit


open class BleSubscriber<T : Any>(val action: ((T) -> Unit)? = null) : Subscriber<T>() {

    override fun onCompleted() {
    }

    override fun onError(e: Throwable?) {
        Log.e("ble-error", "错误", e)
    }

    override fun onNext(t: T) {
        action?.invoke(t)
    }
}

/**
用以返回手环的相关命令
 * Created by tsq on 16/7/31.
 */

abstract class WristDecoder(val commandSender: CommandSender) {
    protected val subscribers = HashMap<String, Subscriber<out Any>?>()

    companion object {
        const val cmd_get_save_value = "cmd_get_save_value"

        const val cmd_get_sport_data = "cmd_get_sport_data"

        const val cmd_write_data = "cmd_write_data"
    }


    /**
     * Subscribes for events of certain type T. Can be called from any thread
     */
    inline fun <reified T : Any> observe(cmd: String, action: () -> Unit): Observable<T> {
        subscribers[cmd]?.let {
            if (it.isUnsubscribed) {
                it.onError(RuntimeException("已经有其它操作占用了"))
            }
            subscribers[cmd] = null
        }
        action.invoke()
        return Observable.create(Observable.OnSubscribe<T> { t -> subscribers[cmd] = t as Subscriber<*> })
                .timeout(5, TimeUnit.SECONDS)
                .finallyDo {
                    subscribers[cmd] = null
                }
                .observeOn(AndroidSchedulers.mainThread())

    }

    fun <T : Any> Subscriber<*>.send(data: T) {
        val subscriber = this as Subscriber<T>
        subscriber.onNext(data)
        subscriber.onCompleted()
    }

    fun Subscriber<*>.success() {
        val subscriber = this as Subscriber<Unit>
        subscriber.onNext(Unit)
        subscriber.onCompleted()
    }


    abstract fun onReceiveData(uuid: UUID, pkgData: ByteArray)


    abstract fun writeData(): Observable<Unit>

    /**
     * 获取存储值，返回存储值
     */
    abstract fun getSaveValue(): Observable<Unit>

    abstract fun getSportData(dayIndex: Int): Observable<WristSportData>
}