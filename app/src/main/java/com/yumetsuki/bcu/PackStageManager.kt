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
import com.yumetsuki.bcu.androidutil.stage.adapters.CustomStageListAdapter
import com.yumetsuki.bcu.androidutil.supports.DynamicListView
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Source.Workspace
import common.util.stage.Stage
import common.util.stage.StageMap
import kotlinx.coroutines.launch

class PackStageManager : AppCompatActivity() {

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
        setContentView(R.layout.activity_pack_stage)

        val result = intent
        val extra = result.extras ?: return

        val map = StaticStore.transformIdentifier<StageMap>(extra.getString("map"))?.get() ?: return

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cusstagebck)
            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)

            val stname = findViewById<TextView>(R.id.cusstagename)
            stname.text = map.toString()

            val adds = findViewById<Button>(R.id.cusstageadd)
            val chlist = findViewById<DynamicListView>(R.id.stageList)

            val adp = CustomStageListAdapter(this@PackStageManager, map)
            chlist.setSwapListener { from, to ->
                map.list.reorder(from, to)
            }
            chlist.adapter = adp

            StaticStore.setDisappear(adds, chlist)
            adds.setOnClickListener {
                val sta = map.add{ Stage(it) }
                adp.add(sta)
            }

            bck.setOnClickListener {
                Workspace.saveWorkspace(false)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackStageManager, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            StaticStore.setAppear(adds, chlist)
            StaticStore.setDisappear(st, prog)
        }
    }
}