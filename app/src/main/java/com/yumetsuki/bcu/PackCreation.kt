package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.PackData.UserPack
import common.pack.UserProfile
import kotlinx.coroutines.launch

class PackCreation : AppCompatActivity() {
    //This page will be used to manage the creation of custom packs

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else {
            if (!shared.getBoolean("theme", false)) {
                setTheme(R.style.AppTheme_night)
            } else {
                setTheme(R.style.AppTheme_day)
            }
        }

        LeakCanaryManager.initCanary(shared, application)
        DefineItf.check(this)
        AContext.check()
        (CommonStatic.ctx as AContext).updateActivity(this)

        //setContentView(R.layout.activity_pack_management)
        lifecycleScope.launch {
            val packList = ArrayList<UserPack>()
            for(pack in UserProfile.getUserPacks())
                if (pack.editable)
                    packList.add(pack)
        }
    }
}