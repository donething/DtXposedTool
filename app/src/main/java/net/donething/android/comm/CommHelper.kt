package net.donething.android.comm

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import de.robv.android.xposed.XposedBridge
import java.io.File

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
                "e" -> Log.e(DEBUG, text, ex ?: Exception("未传递更多异常信息！"))
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

        /**
         * 因为Android N权限变化，需要手动设置权限后，其它应用才能访问
         */
        fun fixSharedPrePermission(activity: Activity) {
            val appDir = File("${activity.filesDir}/../")
            if (appDir.exists()) {
                CommHelper.log("i", "设置App目录权限可读：${appDir.canonicalPath}")
                appDir.setExecutable(true, false)
            } else {
                CommHelper.log("e", "APP目录不存在：${appDir.canonicalPath}", Exception())
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val prefName = File(PreferenceManager.getDefaultSharedPreferencesName(activity))
                val preFile = File("${activity.filesDir}/../shared_prefs/" + prefName + ".xml")
                if (preFile.exists()) {
                    CommHelper.log("i", "设置配置文件权限可读：${preFile.canonicalPath}")
                    preFile.setReadable(true, false)
                } else {
                    CommHelper.log("e", "配置文件不存在：${preFile.canonicalPath}", Exception())
                }
            }
        }

        /**
         * 判断包是否已安装
         */
        fun hadInstalled(activity: Activity, pkname: String): Boolean {
            var packageInfo: PackageInfo? = null
            try {
                packageInfo = activity.packageManager.getPackageInfo(pkname, 0)
            } catch (notFound: PackageManager.NameNotFoundException) {
                CommHelper.log("w", "没有发现指定的包名：$pkname")
            } catch (e: Exception) {
                CommHelper.log("e", "获取包是否已安装错误：${e.message}", e)
            }
            return packageInfo != null
        }

        private val DEBUG = "[DTXPTOOL]"
    }
}
