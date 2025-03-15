package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.enemy.adapters.EnemyListPager
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.pack.PackData
import common.pack.PackData.UserPack
import common.pack.UserProfile
import common.util.stage.MapColc.PackMapColc
import common.util.stage.Stage
import kotlinx.coroutines.launch

class PackStageEnemyManager : AppCompatActivity() {

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
        setContentView(R.layout.activity_pack_stage_enemy)

        val result = intent
        val extra = result.extras ?: return

        val st = StaticStore.transformIdentifier<Stage>(extra.getString("stage"))?.get() ?: return
        val pack = (st.mc as PackMapColc).pack
        val list = st.data

        lifecycleScope.launch {
            val bck = findViewById<FloatingActionButton>(R.id.cussteneback)
            bck.setOnClickListener {
                finish()
            }
            onBackPressedDispatcher.addCallback(this@PackStageEnemyManager, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val tab = findViewById<TabLayout>(R.id.cusstenelisttab)
            val pager = findViewById<ViewPager2>(R.id.enlistpager)

            pager.isSaveEnabled = false
            pager.isSaveFromParentEnabled = false

            val deps : Array<PackData> = Array(pack.desc.dependency.size + 2) {
                when (it) {
                    0 -> UserProfile.getBCData()
                    1 -> pack
                    else -> UserProfile.getUserPack(pack.desc.dependency[it - 2])
                }
            }
            pager.adapter = EnemyListTab(deps)
            pager.offscreenPageLimit = deps.size

            TabLayoutMediator(tab, pager) { t, position ->
                t.text = if(position == 0)
                    getString(R.string.pack_default)
                else
                    (deps[position] as UserPack).desc.names.toString().ifBlank { deps[position].sid }
            }.attach()
        }
    }

    inner class EnemyListTab(private val deps : Array<PackData>) : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return deps.size
        }

        override fun createFragment(position: Int): Fragment {
            return EnemyListPager.newInstance(deps[position].sid, position, EnemyList.Mode.SELECTION)
        }
    }
}