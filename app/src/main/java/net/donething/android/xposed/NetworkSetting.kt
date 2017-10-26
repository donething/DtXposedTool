package net.donething.android.xposed

import android.content.Context
import android.os.Build
import android.os.Message
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper


// Created by Donething on 2017-09-21.

class NetworkSetting {
    companion object {
        /**
         * 设置优先网络类型
         */
        fun dealPreferredNetwork(lpparam: XC_LoadPackage.LoadPackageParam) {
            MyXposed.xsp.reload()
            val networkType = MyXposed.xsp.getString("network_preferred", "-1").toInt()
            if (networkType == -1) return

            var networkTypeClass: Class<*>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkTypeClass = XposedHelpers.findClass("com.android.internal.telephony.Phone", lpparam.classLoader)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                networkTypeClass = XposedHelpers.findClass("com.android.internal.telephony.RIL", lpparam.classLoader)
            }

            XposedHelpers.findAndHookMethod(networkTypeClass, "setPreferredNetworkType", Int::class.java, Message::class.java, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    // CommHelper.log("i", "当前包：${lpparam.packageName}")
                    CommHelper.log("i", "更改原网络类型：${param.args[0]}，为：$networkType")
                    param.args[0] = networkType
                    val context = XposedHelpers.findField(networkTypeClass, "mContext").get(param.thisObject) as Context
                }
            })
        }
    }
}