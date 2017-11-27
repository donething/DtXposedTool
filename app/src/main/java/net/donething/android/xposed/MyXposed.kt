package net.donething.android.xposed

import android.content.res.XModuleResources
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper
import net.donething.android.dtxposedtool.BuildConfig

// Created by Donething on 2017-09-14.

class MyXposed : IXposedHookLoadPackage, IXposedHookInitPackageResources, IXposedHookZygoteInit {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        xsp.reload()
        xsp.makeWorldReadable()

        // 后台安装应用
        if (lpparam.packageName == "com.android.packageinstaller" || lpparam.packageName == "com.google.android.packageinstaller") {
            CommHelper.log("i", "开始Hook包：packageInstaller：${lpparam.packageName}")
            SystemSetting.dealPackageInstaller(lpparam)
        }

        // 隐藏媒体音量面板、准许真机连接Hierarchy
        if (lpparam.packageName == "android" && lpparam.processName == "android") {
            CommHelper.log("i", "开始Hook包：android；${lpparam.packageName}")
            SystemSetting.dealHideMediaVolPanel(lpparam)    // 隐藏媒体音量面板
            SystemSetting.dealEnableHierarchy(lpparam)      // 准许真机连接Hierarchy
        }

        // 设置网络优先级，因为对应的包特多且无法指定哪些，所以此处不判断包名
        NetworkSetting.dealPreferredNetwork(lpparam)

        // 优酷后台播放
        if (lpparam.packageName == "com.youku.phone") {
            CommHelper.log("i", "开始Hook包：优酷：${lpparam.packageName}")
            Youku.dealYouku(lpparam)
        }

        // Iqiyi
        if (lpparam.packageName == "com.qiyi.video") {
            CommHelper.log("i", "开始Hook包：爱奇艺：${lpparam.packageName}")
            // Iqiyi.dealIqiyi(lpparam)
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        // 为了将图片资源注入进待Hook住的应用中
        if (!hadInjectRes && resparam.packageName == "com.android.packageinstaller"
                || resparam.packageName == "com.google.android.packageinstaller") {
            val xModeRes = XModuleResources.createInstance(MODULE_PATH, resparam.res)
            SystemSetting.addResource(resparam, xModeRes)
            hadInjectRes = true
        }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        MODULE_PATH = startupParam.modulePath
    }

    private var MODULE_PATH: String? = null
    private var hadInjectRes = false

    companion object {
        val xsp = XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_preferences")
    }
}