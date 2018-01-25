package net.donething.android.xposed

import android.content.Context
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper


// Created by Donething on 2017-09-14.

object Youku {
    fun dealYouku(lpparam: XC_LoadPackage.LoadPackageParam) {
        val detailActivityClass = XposedHelpers.findClass("com.youku.ui.activity.DetailActivity", lpparam.classLoader)

        // 拦截Activity的onPause，可阻止播放器暂停
        XposedHelpers.findAndHookMethod(detailActivityClass, "onPause", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam) {
                CommHelper.log("i", "开始拦截${detailActivityClass.canonicalName}.onPause()")
                XposedHelpers.callMethod(param.thisObject, "onResume")
                param.result = null
            }
        })

        /* 已经实现了返回键退出而后台播放，所以此方法已废弃
        XposedHelpers.findAndHookMethod(detailActivityClass, "onStop", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: XC_MethodHook.MethodHookParam?) {
                CommHelper.log("i", "开始拦截${detailActivityClass.canonicalName}.onStop()")
                // 播放器是否处于暂停状态
                val isPlayerOnPause = isPlayerOnPauseField.getBoolean(param!!.thisObject)
                if (isPlayerOnPause) {
                    XposedHelpers.callMethod(param.thisObject, "onPause")
                }
            }
        })
        */

        // 解决卸载软件时优酷FC的问题
        XposedHelpers.findAndHookMethod("com.youku.gamecenter.GameCenterModel", lpparam.classLoader, "handlePackageRemoved", Context::class.java, String::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                param.result = null
            }
        })
    }
}