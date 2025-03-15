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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.stage.adapters.CustomStageListAdapter
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.Source.Workspace
import common.util.stage.MapColc.PackMapColc
import common.util.stage.Stage
import common.util.stage.StageMap
import common.util.stage.info.CustomStageInfo
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
            val chlist = findViewById<RecyclerView>(R.id.stageList)
            chlist.layoutManager = LinearLayoutManager(this@PackStageManager)
            val adp = CustomStageListAdapter(this@PackStageManager, map)
            chlist.adapter = adp
            val touch = ItemTouchHelper(object: ItemTouchHelper.Callback() {
                override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                    return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.END)
                }
                override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                    val from = src.bindingAdapterPosition
                    val to = dest.bindingAdapterPosition
                    map.list.reorder(from, to)
                    adp.notifyItemMoved(from, to)
                    return false
                }
                override fun onSwiped(holder: RecyclerView.ViewHolder, j: Int) {
                    val pos = holder.bindingAdapterPosition
                    val sta = map.list[pos]

                    map.list.remove(sta)
                    if (sta.info != null)
                        (sta.info as CustomStageInfo).destroy(false)
                    for (si in (sta.mc as PackMapColc).si)
                        si.remove(sta)
                    adp.notifyItemRemoved(pos)
                }
            })
            touch.attachToRecyclerView(chlist)

            StaticStore.setDisappear(adds, chlist)
            adds.setOnClickListener {
                val sta = map.add{ Stage(it) }
                adp.notifyItemInserted(map.list.indexOf(sta))
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