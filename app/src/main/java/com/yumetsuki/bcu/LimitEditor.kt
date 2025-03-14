package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AppCompatActivity
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.util.stage.Limit
import kotlinx.coroutines.launch

class LimitEditor : AppCompatActivity() {

    companion object {
        lateinit var lim : Limit
    }

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
        setContentView(R.layout.activity_pack_limit)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val lnum : EditText = findViewById(R.id.pklimmax)
            lnum.text = SpannableStringBuilder(lim.num.toString())
            lnum.setOnEditorActionListener {_, actionId, _ ->
                val num = CommonStatic.parseIntN(lnum.text.toString())
                if (actionId == EditorInfo.IME_ACTION_NONE || num == lim.num)
                    return@setOnEditorActionListener false
                lim.num = num
                false
            }

            val lminc : EditText = findViewById(R.id.pklimmincost)
            lminc.text = SpannableStringBuilder(lim.min.toString())
            lminc.setOnEditorActionListener {_, actionId, _ ->
                val min = CommonStatic.parseIntN(lminc.text.toString())
                if (actionId == EditorInfo.IME_ACTION_NONE || min == lim.min)
                    return@setOnEditorActionListener false
                lim.min = min
                false
            }

            val lmaxc : EditText = findViewById(R.id.pklimmaxcost)
            lmaxc.text = SpannableStringBuilder(lim.max.toString())
            lmaxc.setOnEditorActionListener {_, actionId, _ ->
                val max = CommonStatic.parseIntN(lmaxc.text.toString())
                if (actionId == EditorInfo.IME_ACTION_NONE || max == lim.max)
                    return@setOnEditorActionListener false
                lim.max = max
                false
            }

            setD
        }
    }
}