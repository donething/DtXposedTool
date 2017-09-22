package net.donething.android.dtxposedtool

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // CommHelper.log("i", "当前移动网络模式：" + Settings.Global.getInt(contentResolver, "preferred_network_mode"))
        // 显示 PreferenceFragment
        fragmentManager.beginTransaction().replace(R.id.content, PreferencesFragment()).commit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {
                return exitConfirm()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 退出确认
     */
    private fun exitConfirm(): Boolean {
        if (System.currentTimeMillis() - exitTime <= 1500) {
            this.finish()
        } else {
            Toast.makeText(this, "再次返回退出应用", Toast.LENGTH_SHORT).show()
            exitTime = System.currentTimeMillis()
        }
        return true
    }

    private var exitTime = 0L   // 保存返回键按键时间（毫秒），用于退出确认
}
