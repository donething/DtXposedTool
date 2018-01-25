package net.donething.android.xposed

import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.XModuleResources
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import net.donething.android.comm.CommHelper
import net.donething.android.dtxposedtool.R

// Created by Donething on 2017-09-16.

object SystemSetting {
    // Xposed：后台安装
    /**
     * 后台安装
     */
    fun dealPackageInstaller(lpparam: XC_LoadPackage.LoadPackageParam) {
        val installAppProgressClass = XposedHelpers.findClass("com.android.packageinstaller.InstallAppProgress", lpparam.classLoader)
        if (!MyXposed.xsp.getBoolean("enable_bg_install", true)) return

        // 隐藏软件安装界面
        findAndHookMethod(installAppProgressClass, "onCreate", Bundle::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                try {
                    val activity = param.thisObject as Activity
                    // 获取包含了安装包信息的Filed:mAppInfo
                    val mLabel = (XposedHelpers.findField(installAppProgressClass, "mLabel").get(activity) as CharSequence).toString()

                    CommHelper.log("i", "finish()PackageInstaller的Activity，后台安装：$mLabel")
                    Toast.makeText(activity, "后台安装：$mLabel", Toast.LENGTH_SHORT).show()
                    activity.finish()     // finish()以隐藏Activity
                } catch (ex: Exception) {
                    CommHelper.logXp("Hook：${installAppProgressClass::javaClass}.onCreate()时发生异常：$ex")
                }
            }
        })
        // 软件安装完后，弹出通知提示
        // Android6.0.1以下
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            findAndHookMethod("com.android.packageinstaller.InstallAppProgress${"$"}PackageInstallObserver", lpparam.classLoader,
                    "packageInstalled", String::class.java, Int::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = XposedHelpers.getSurroundingThis(param.thisObject) as Activity    // 获取外部类的对象
                    val mLabel = activity.packageManager.getApplicationLabel(activity.packageManager.getApplicationInfo(param.args[0].toString(), 0))

                    CommHelper.log("i", "应用安装结果：${param.args.toList()}")
                    val title = "$mLabel 安装${if (param.args[1] == 1) "成功" else "失败"}"
                    val text = if (param.args[1] == 1) "运行 $mLabel" else "失败原因：${XposedHelpers.callMethod(activity, "getExplanationFromErrorCode", param.args[1])}"
                    makeNotification(activity, title, text, param.args[0].toString(), param.args[1] == 1)
                }
            })
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            findAndHookMethod("com.android.packageinstaller.InstallAppProgress${"$"}2", lpparam.classLoader, "onReceive", Context::class.java, Intent::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    CommHelper.log("i", "应用安装完成的broadcast")
                    // val message = param.args[0] as Message
                }
            })
        }
    }

    /**
     * 应用安装完成后，发送通知
     */
    private fun makeNotification(activity: Activity, title: String, text: String, packageName: String, isInstallSuccess: Boolean) {
        try {
            val icon = activity.packageManager.getApplicationIcon(packageName)  // 获取包名对应的drawable图标对象

            val noManager = activity.getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
            val noRunIntent = PendingIntent.getActivity(activity, noID, activity.packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_UPDATE_CURRENT)

            val builder = Notification.Builder(activity).setShowWhen(true)
                    .setLargeIcon((icon as BitmapDrawable).bitmap)
                    .setSmallIcon(noIconResID)  // 设置通过xposed注入的图标
                    .setContentTitle(title)
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setNumber(noID)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVibrate(longArrayOf(0, 10))
            if (isInstallSuccess) {
                builder.setContentIntent(noRunIntent)
                // builder.addAction(NotificationCompat.Action(noRunIconResID, "运行应用", noRunIntent))
            }
            noManager.notify(noID++, builder.build())
        } catch (e: Exception) {
            CommHelper.logXp("创建通知出错：${e.message}")
        }
    }

    // Xposed：隐藏媒体音量面板
    /**
     * 隐藏媒体音量调节面板
     */
    fun dealHideMediaVolPanel(lpparam: XC_LoadPackage.LoadPackageParam) {
        MyXposed.xsp.reload()
        if (!MyXposed.xsp.getBoolean("hidden_media_volpanel", true)) return
        val audioServiceClass = XposedHelpers.findClass("com.android.server.audio.AudioService", lpparam.classLoader)
        findAndHookMethod(audioServiceClass, "sendVolumeUpdate", Int::class.java, Int::class.java, Int::class.java,
                Int::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                // 因为该方法会被调用两次，所以只取不相等的那次
                if (param.args[1] != param.args[2]) CommHelper.log("i", "音量信息：${param.args.toList()}")

                if (param.args[0] == AudioManager.STREAM_MUSIC) {
                    CommHelper.log("i", "隐藏媒体音量调节面板")
                    param.args[3] = AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE
                }

                if (isMinVolume(param.args[1].toString().toInt(), param.args[2].toString().toInt())) {
                    val mContext = XposedHelpers.findField(audioServiceClass, "mContext").get(param.thisObject) as Context
                    val mAudioHandler = XposedHelpers.findField(audioServiceClass, "mAudioHandler").get(param.thisObject) as Handler
                    // 因为本服务属于子线程，无法显示Toast，所以通过handler来post()到主线程中显示
                    mAudioHandler.post({
                        Toast.makeText(mContext, "当前音量：${param.args[2]}", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        })
    }

    /**
     * 判断是否已是最小音量（非无声）
     */
    private fun isMinVolume(old: Int, current: Int): Boolean {
        // 音量值不相等，且最小音量为0，或
        return (old != current && Math.min(old, current) == 0 || old - 2 * current == 0)
    }

    // Xposed：准许真机连接Hierarchy
    fun dealEnableHierarchy(lpparam: XC_LoadPackage.LoadPackageParam) {
        MyXposed.xsp.reload()
        if (!MyXposed.xsp.getBoolean("enable_connect_hierarchy", false)) return
        findAndHookMethod("com.android.server.wm.WindowManagerService", lpparam.classLoader,
                "isSystemSecure", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                CommHelper.log("i", "Hook isSystemSecure()，设置返回值为false")
                param.result = false
            }
        })
    }

    fun enableView(lpparam: XC_LoadPackage.LoadPackageParam) {
        XposedBridge.hookAllMethods(View::class.java, "onClick", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam?) {
                CommHelper.log("i", "点击了控件$param！")
            }
        })
    }

    /**
     * 将资源注入到待Hook的应用中
     */
    fun addResource(resparam: XC_InitPackageResources.InitPackageResourcesParam, xModeRes: XModuleResources) {
        CommHelper.log("i", "向${resparam.packageName} 注入资源文件")
        noIconResID = resparam.res.addResource(xModeRes, R.drawable.no_app_installed)
        noRunIconResID = resparam.res.addResource(xModeRes, R.drawable.no_run_app)
    }

    private var noIconResID = 0     // 注入的资源的ID
    private var noRunIconResID = 0  // 运行应用的资源ID
    private var noID = 1    // 应用安装时发送的初始通知ID，共3处引用，只在最后一个引用处执行 noID++
}