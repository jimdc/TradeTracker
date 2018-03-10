package com.advent.group69.tradetracker
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    companion object KEY_PREF_EXAMPLE_SWITCH {
        fun get() = "example_switch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mFragmentTransaction = supportFragmentManager.beginTransaction()
        mFragmentTransaction.replace(android.R.id.content, SettingsFragment())
        mFragmentTransaction.commit()
    }

}
