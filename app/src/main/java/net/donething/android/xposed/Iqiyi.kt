package net.donething.android.xposed

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper

/**
 * Created by zl on 17-11-25.
 */
object Iqiyi {
    fun dealIqiyi(lpparam: XC_LoadPackage.LoadPackageParam) {
        val playerActivityClass = XposedHelpers.findClass("org.iqiyi.video.activity.PlayerActivity", lpparam.classLoader)
        XposedHelpers.findAndHookMethod(playerActivityClass, "onPause", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                CommHelper.log("i", "开始拦截${playerActivityClass.canonicalName}.onPause()")
                // 播放器是否处于暂停状态
                XposedHelpers.callMethod(param.thisObject, "onResume")
                param.result = null
            }
        })
    }
}