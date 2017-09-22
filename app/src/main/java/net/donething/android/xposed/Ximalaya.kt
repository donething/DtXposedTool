package net.donething.android.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper

// Created by Donething on 2017-09-14.

class Ximalaya {
    companion object {
        fun dealXimalaya(lpparam: XC_LoadPackage.LoadPackageParam) {
            XposedHelpers.findAndHookMethod("com.ximalaya.ting.android.fragment.play.PlayFragment", lpparam.classLoader,
                    "onPause", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    CommHelper.log("i", "hello，喜马拉雅。")
                }
            })
        }
    }
}