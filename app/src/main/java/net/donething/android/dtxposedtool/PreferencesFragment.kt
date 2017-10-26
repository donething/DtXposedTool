package net.donething.android.dtxposedtool

import android.os.Bundle
import android.preference.PreferenceFragment
import net.donething.android.comm.CommHelper

// Created by Donething on 2017-09-21.

class PreferencesFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 使配置文件可被其它应用读取
        // 此语句一定要放在addPreferencesFromResource()前面才有效
        addPreferencesFromResource(R.xml.fragment_preferences)
    }

    override fun onStart() {
        CommHelper.fixSharedPrePermission(activity)
        super.onStart()
    }

    override fun onPause() {
        CommHelper.fixSharedPrePermission(activity)
        super.onPause()
    }
}