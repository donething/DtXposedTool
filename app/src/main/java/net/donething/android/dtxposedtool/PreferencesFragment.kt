package net.donething.android.dtxposedtool

import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen

// Created by Donething on 2017-09-21.

class PreferencesFragment : PreferenceFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferenceManager.sharedPreferencesMode = Context.MODE_WORLD_READABLE   // 此语句一定要放在addPreferencesFromResource()前面才有效
        addPreferencesFromResource(R.xml.fragment_preferences)
    }

    override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen, preference: Preference): Boolean {
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }
}