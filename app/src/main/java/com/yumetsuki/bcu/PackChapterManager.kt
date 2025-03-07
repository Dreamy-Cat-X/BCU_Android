package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.stage.adapters.CustomChapterListAdapter
import com.yumetsuki.bcu.androidutil.supports.DynamicListView
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Source.Workspace
import common.pack.UserProfile
import common.util.stage.StageMap
import kotlinx.coroutines.launch

class PackChapterManager : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.clear()
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        if (!shared.contains("initial")) {
            val ed = shared.edit()
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
        setContentView(R.layout.activity_pack_chapter)

        val result = intent
        val extra = result.extras

        val pack = UserProfile.getUserPack(extra?.getString("pack")) ?: return

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cuschapterbck)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            val addc = findViewById<Button>(R.id.cuschapteradd)
            val chlist = findViewById<DynamicListView>(R.id.chapterList)
            StaticStore.setDisappear(addc, chlist)

            val adp = CustomChapterListAdapter(pack, this@PackChapterManager)
            chlist.setSwapListener { from, to ->
                pack.mc.maps.reorder(from, to)
            }
            chlist.adapter = adp

            addc.setOnClickListener {
                val map = pack.mc.add{ StageMap(it) }
                adp.add(map)
            }

            bck.setOnClickListener {
                Workspace.saveWorkspace(false)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackChapterManager, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            StaticStore.setAppear(addc, chlist)
            StaticStore.setDisappear(st, prog)
        }
    }
}