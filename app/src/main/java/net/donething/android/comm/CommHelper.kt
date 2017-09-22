package net.donething.android.comm

import android.util.Log
import de.robv.android.xposed.XposedBridge

// Created by Donething on 2017-09-14.

class CommHelper {
    companion object {
        /**
         * 只记录有关XPosed的错误日志
         */
        fun logXp(error: String) {
            XposedBridge.log("$DEBUG $error")
        }

        /**
         * 记录日志
         * @param type 分4个等级：i,w,e,v
         * @param text 说明信息
         * @param ex 异常（默认为null）
         */
        fun log(type: String, text: String, ex: Exception? = null) {
            when (type) {
                "i" -> Log.i(DEBUG, text)
                "w" -> Log.w(DEBUG, text)
                "e" -> Log.e(DEBUG, text, ex ?: Exception("No more exception info."))
                else -> Log.v(DEBUG, text)
            }
        }

        /**
         * 通过反射获取类域
         * @param clazz 需获取域的class
         * @param obj class对应的实例，默认null时表示需要自己实例化class获得对象
         * @param fieldName 需要获取的域
         */
        fun getField(className: String, obj: Any? = null, fieldName: String): Any? {
            try {
                val clazz = Class.forName(className)
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(obj ?: clazz.newInstance())
            } catch (ex: Exception) {
                log("e", "反射获取域时出现异常", ex)
            }
            return null
        }

        private val DEBUG = "[DTXPTOOL]"
    }
}
