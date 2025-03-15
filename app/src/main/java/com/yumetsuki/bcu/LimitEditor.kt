package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableRow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import common.CommonStatic
import common.util.stage.Limit
import common.util.stage.StageLimit
import kotlinx.coroutines.launch
import kotlin.math.max

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
            val bck = findViewById<FloatingActionButton>(R.id.cuslimbck)
            bck.setOnClickListener {
                finish()
            }
            onBackPressedDispatcher.addCallback(this@LimitEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val lname : TextView = findViewById(R.id.cuslimname)
            lname.text = extra.getString("name", lim.toString())

            val lnum : EditText = findViewById(R.id.pklimmax)
            lnum.text = SpannableStringBuilder(lim.num.toString())
            lnum.setOnEditorActionListener { _, _, _ ->
                val num = CommonStatic.parseIntN(lnum.text.toString())
                if (num == lim.num)
                    return@setOnEditorActionListener false
                lim.num = num
                false
            }

            val lminc : EditText = findViewById(R.id.pklimmincost)
            lminc.text = SpannableStringBuilder(lim.min.toString())
            lminc.setOnEditorActionListener { _, _, _ ->
                val min = CommonStatic.parseIntN(lminc.text.toString())
                if (min == lim.min)
                    return@setOnEditorActionListener false
                lim.min = min
                false
            }

            val lmaxc : EditText = findViewById(R.id.pklimmaxcost)
            lmaxc.text = SpannableStringBuilder(lim.max.toString())
            lmaxc.setOnEditorActionListener { _, _, _ ->
                val max = CommonStatic.parseIntN(lmaxc.text.toString())
                if (max == lim.max)
                    return@setOnEditorActionListener false
                lim.max = max
                false
            }

            val lstar : EditText = findViewById(R.id.pklimstar)
            lstar.text = SpannableStringBuilder(lim.toString())
            lstar.setOnEditorActionListener {_, actionId, _ ->
                val stars = CommonStatic.parseIntsN(lstar.text.toString())
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED)
                    return@setOnEditorActionListener false
                lim.star = 0
                for (i in stars)
                    if (i < 0 || i > 3)
                        lim.star = lim.star or (1 shl i)
                false
            }

            val lrow : Button = findViewById(R.id.pklimrow)
            lrow.text = getString(when (lim.line) {
                1 -> R.string.limit_line2
                2 -> R.string.limit_line3
                else -> R.string.limit_linen
            })
            lrow.setOnClickListener {
                lim.line = (lim.line + 1) % 3
                lrow.text = getString(when (lim.line) {
                    1 -> R.string.limit_line2
                    2 -> R.string.limit_line3
                    else -> R.string.limit_linen
                })
            }

            val lrars : LinearLayout = findViewById(R.id.pklimrars)
            for (i in 0 until lrars.childCount) {
                val rar = lrars.getChildAt(i) as CheckBox
                rar.isChecked = lim.rare == 0 || ((lim.rare shr i) and 1) > 0

                rar.setOnClickListener {
                    lim.rare = lim.rare xor (1 shl i)
                    for (j in 0 until lrars.childCount) {
                        val brar = lrars.getChildAt(j) as CheckBox
                        brar.isChecked = lim.rare == 0 || ((lim.rare shr j) and 1) > 0
                    }
                }
            }

            val lcgroup : Button = findViewById(R.id.pklimcharagroup)
            val t = "${getString(R.string.limit_chra)}: ${lim.group}"
            lcgroup.text = t
            lcgroup.setOnClickListener {
                //TODO("WIP")
            }

            val llvr : Button = findViewById(R.id.pklimlvrestriction)
            val u = "${getString(R.string.limit_lvres)}: ${lim.lvr}"
            llvr.text = u
            llvr.setOnClickListener {
                //TODO("WIP")
            }

            if (lim.stageLimit == null)
                lim.stageLimit = StageLimit()

            val lmaxu : EditText = findViewById(R.id.pklimbank)
            lmaxu.text = SpannableStringBuilder(lim.stageLimit.maxMoney.toString())
            lmaxu.setOnEditorActionListener { _, _, _ ->
                val num = CommonStatic.parseIntN(lmaxu.text.toString())
                if (num == lim.stageLimit.maxMoney)
                    return@setOnEditorActionListener false
                lim.stageLimit.maxMoney = num
                false
            }
            val lcd : EditText = findViewById(R.id.pklimucd)
            lcd.text = SpannableStringBuilder(lim.stageLimit.globalCooldown.toString())
            lcd.setOnEditorActionListener { _, _, _ ->
                val num = CommonStatic.parseIntN(lcd.text.toString())
                if (num == lim.stageLimit.globalCooldown)
                    return@setOnEditorActionListener false
                lim.stageLimit.globalCooldown = num
                false
            }
            val lunico : EditText = findViewById(R.id.pklimunico)
            lunico.text = SpannableStringBuilder(lim.stageLimit.globalCost.toString())
            lunico.setOnEditorActionListener { _, _, _ ->
                val num = CommonStatic.parseIntN(lunico.text.toString())
                if (num == lim.stageLimit.globalCost)
                    return@setOnEditorActionListener false
                lim.stageLimit.globalCost = num
                false
            }
            val lmaxd : EditText = findViewById(R.id.pklimmaxdeploy)
            lmaxd.text = SpannableStringBuilder(lim.stageLimit.maxUnitSpawn.toString())
            lmaxd.setOnEditorActionListener { _, _, _ ->
                val num = CommonStatic.parseIntN(lmaxd.text.toString())
                if (num == lim.stageLimit.maxUnitSpawn)
                    return@setOnEditorActionListener false
                lim.stageLimit.maxUnitSpawn = num
                false
            }
            val lscd : CheckBox = findViewById(R.id.pklimscd)
            lscd.isChecked = lim.stageLimit.coolStart
            lscd.setOnClickListener { lim.stageLimit.coolStart = lscd.isChecked }

            val lcosm : LinearLayout = findViewById(R.id.pklimcosm)
            for (i in 1 until lcosm.childCount) {
                val cosm = lcosm.getChildAt(i) as EditText
                cosm.text = SpannableStringBuilder(lim.stageLimit.costMultiplier[i-1].toString())
                cosm.setOnEditorActionListener { _, _, _ ->
                    val num = CommonStatic.parseIntN(cosm.text.toString())
                    if (num == lim.stageLimit.costMultiplier[i-1])
                        return@setOnEditorActionListener false
                    lim.stageLimit.costMultiplier[i-1] = num
                    false
                }
            }
            val lcdm : LinearLayout = findViewById(R.id.pklimcdm)
            for (i in 1 until lcdm.childCount) {
                val cdm = lcdm.getChildAt(i) as EditText
                cdm.text = SpannableStringBuilder(lim.stageLimit.cooldownMultiplier[i-1].toString())
                cdm.setOnEditorActionListener { _, _, _ ->
                    val num = CommonStatic.parseIntN(cdm.text.toString())
                    if (num == lim.stageLimit.cooldownMultiplier[i-1])
                        return@setOnEditorActionListener false
                    lim.stageLimit.cooldownMultiplier[i-1] = num
                    false
                }
            }
            val lrspwn : LinearLayout = findViewById(R.id.pklimrspwn)
            for (i in 1 until lrspwn.childCount) {
                val rspwn = lrspwn.getChildAt(i) as EditText
                rspwn.text = SpannableStringBuilder(lim.stageLimit.rarityDeployLimit[i-1].toString())
                rspwn.setOnEditorActionListener { _, _, _ ->
                    val num = max(-1, CommonStatic.parseIntN(rspwn.text.toString()))
                    if (num == lim.stageLimit.rarityDeployLimit[i-1])
                        return@setOnEditorActionListener false
                    lim.stageLimit.rarityDeployLimit[i-1] = num
                    rspwn.text = SpannableStringBuilder(lim.stageLimit.rarityDeployLimit[i-1].toString())
                    false
                }
            }
            val ldpspwn : LinearLayout = findViewById(R.id.pklimdpspwn)
            for (i in 1 until ldpspwn.childCount) {
                val dpspwn = ldpspwn.getChildAt(i) as TableRow

                val dpcount = dpspwn.getChildAt(0) as EditText
                dpcount.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationTimes[i-1].toString())
                val dptime = dpspwn.getChildAt(2) as EditText
                dptime.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationDelay[i-1].toString())

                dpcount.setOnEditorActionListener { _, _, _ ->
                    val num = CommonStatic.parseIntN(dpcount.text.toString())
                    if (num == lim.stageLimit.deployDuplicationTimes[i-1])
                        return@setOnEditorActionListener false
                    lim.stageLimit.deployDuplicationTimes[i-1] = num
                    if (num == 0) {
                        lim.stageLimit.deployDuplicationTimes[i-1] = 0
                        dptime.text = SpannableStringBuilder("0")
                    } else if (lim.stageLimit.deployDuplicationDelay[i - 1] == 0) {
                        lim.stageLimit.deployDuplicationTimes[i-1] = 1
                        dptime.text = SpannableStringBuilder("1")
                    }
                    false
                }
                dptime.setOnEditorActionListener { _, _, _ ->
                    var num = CommonStatic.parseIntN(dptime.text.toString())
                    if (num == lim.stageLimit.deployDuplicationDelay[i-1])
                        return@setOnEditorActionListener false
                    num = if (lim.stageLimit.deployDuplicationTimes[i-1] == 0) 0 else max(num, 1)

                    lim.stageLimit.deployDuplicationDelay[i-1] = num
                    dptime.text = SpannableStringBuilder(lim.stageLimit.deployDuplicationDelay[i-1].toString())
                    false
                }
            }

            val st = findViewById<TextView>(R.id.status)
            val prog = findViewById<ProgressBar>(R.id.prog)
            StaticStore.setDisappear(st, prog)
        }
    }
}