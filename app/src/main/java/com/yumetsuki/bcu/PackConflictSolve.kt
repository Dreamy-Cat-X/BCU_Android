package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.androidutil.LocaleManager
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.io.ErrorLogWriter
import com.yumetsuki.bcu.androidutil.pack.PackConflict
import com.yumetsuki.bcu.androidutil.pack.conflict.adapters.PackConfListAdapter
import com.yumetsuki.bcu.androidutil.pack.conflict.asynchs.PackConfSolver
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import java.util.*
import kotlin.collections.ArrayList

class PackConflictSolve : AppCompatActivity() {
    companion object {
        const val RESULT_OK = 801
        val data = ArrayList<Int>()
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this::onResult)

    private fun onResult(result: ActivityResult) {
        if(result.resultCode == RESULT_OK && dataChanged()) {
            val pclist = findViewById<ListView>(R.id.packconflist)
            val names = ArrayList<String>()

            for(pc in PackConflict.conflicts) {
                names.add(pc.toString())
            }

            val adapter = PackConfListAdapter(this, names)

            pclist.adapter = adapter

            pclist.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
                val intent = Intent(this, PackConflictDetail::class.java)

                intent.putExtra("position", position)

                resultLauncher.launch(intent)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
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
        Thread.setDefaultUncaughtExceptionHandler(ErrorLogWriter())
        setContentView(R.layout.activity_pack_conflict_solve)

        val bck = findViewById<FloatingActionButton>(R.id.packconfbck)
        val pclist = findViewById<ListView>(R.id.packconflist)
        val prog = findViewById<ProgressBar>(R.id.packconfprog)
        val solve = findViewById<Button>(R.id.packconfsolve)

        solve.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                PackConfSolver(this@PackConflictSolve).execute()
            }

        })

        prog.visibility = View.GONE

        bck.setOnClickListener {
            finish()
        }

        val names = ArrayList<String>()

        for(pc in PackConflict.conflicts) {
            names.add(pc.toString())
        }

        if(data.isEmpty()) {
            for(pc in PackConflict.conflicts) {
                data.add(pc.action)
            }
        }

        val adapter = PackConfListAdapter(this, names)

        pclist.adapter = adapter

        pclist.onItemClickListener = AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            val intent = Intent(this, PackConflictDetail::class.java)

            intent.putExtra("position", position)

            resultLauncher.launch(intent)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                bck.performClick()
            }
        })
    }

    override fun attachBaseContext(newBase: Context) {
        val shared = newBase.getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val lang = shared?.getInt("Language",0) ?: 0

        val config = Configuration()
        var language = StaticStore.lang[lang]
        var country = ""

        if(language == "") {
            language = Resources.getSystem().configuration.locales.get(0).language
            country = Resources.getSystem().configuration.locales.get(0).country
        }

        val loc = if(country.isNotEmpty()) {
            Locale(language, country)
        } else {
            Locale(language)
        }

        config.setLocale(loc)
        applyOverrideConfiguration(config)
        super.attachBaseContext(LocaleManager.langChange(newBase,shared?.getInt("Language",0) ?: 0))
    }

    override fun onDestroy() {
        super.onDestroy()
        StaticStore.toast = null
    }

    private fun dataChanged() : Boolean {
        for(d in data.indices) {
            val pc = PackConflict.conflicts[d]

            if(data[d] != pc.action) {
                data.clear()

                for(p in PackConflict.conflicts)
                    data.add(p.action)

                return true
            }

        }

        return false
    }

    override fun onResume() {
        AContext.check()

        if(CommonStatic.ctx is AContext)
            (CommonStatic.ctx as AContext).updateActivity(this)

        super.onResume()
    }
}