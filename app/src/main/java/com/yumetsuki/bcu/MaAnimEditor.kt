package com.yumetsuki.bcu

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.JsonParser
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.animation.AnimationEditView
import com.yumetsuki.bcu.androidutil.animation.adapter.MaModelListAdapter
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.MaAnim
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class MaAnimEditor : AppCompatActivity() {

    companion object {
        var tempFunc : ((input: MaAnim) -> Unit)? = null
    }

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == RESULT_OK) {
            val path = result.data?.data ?: return@registerForActivityResult

            Log.i("AnimationManagement", "Got URI : $path")
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)

            val resolver = applicationContext.contentResolver
            val cursor = try {
                resolver.query(path, projection, null, null, null)
            } catch (_: SecurityException) {
                StaticStore.showShortMessage(this, R.string.pack_import_denied)
                return@registerForActivityResult
            } catch (_: FileNotFoundException) {
                StaticStore.showShortMessage(this, R.string.pack_import_nofile)
                return@registerForActivityResult
            } catch (_: IllegalArgumentException) {
                StaticStore.showShortMessage(this, R.string.pack_import_nofile)
                return@registerForActivityResult
            }

            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    val name = cursor.getString(0) ?: return@registerForActivityResult

                    if(name.endsWith(".txt") || name.endsWith(".mamodel")) {
                        val fl = File(StaticStore.getExternalPack(this), name)
                        if (fl.exists()) {
                            val imc = MaAnim.newIns(FDFile(fl), false)
                            tempFunc?.invoke(imc)
                        }
                    }
                }
                cursor.close()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shared = getSharedPreferences(StaticStore.CONFIG, Context.MODE_PRIVATE)
        val ed: SharedPreferences.Editor

        if (!shared.contains("initial")) {
            ed = shared.edit()
            ed.putBoolean("initial", true)
            ed.putBoolean("theme", true)
            ed.apply()
        } else if (!shared.getBoolean("theme", false))
            setTheme(R.style.AppTheme_night)
        else
            setTheme(R.style.AppTheme_day)

        LeakCanaryManager.initCanary(shared, application)
        DefineItf.check(this)
        AContext.check()
        (CommonStatic.ctx as AContext).updateActivity(this)
        //setContentView(R.layout.activity_maanim_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            /*val root = findViewById<ConstraintLayout>(R.id.maanimroot)
            val layout = findViewById<LinearLayout>(R.id.maanimlayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.maanimCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.maanimMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.maanimCfgHide)

            StaticStore.setDisappear(cfgBtn, layout)*/

            val res = JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), ResourceLocation::class.java) ?: return@launch
            val anim = if (res.pack == ResourceLocation.LOCAL)
                AnimCE.map()[res.id]
            else
                UserProfile.getUserPack(res.pack)?.source?.loadAnimation(res.id, res.base) as AnimCE?
            if (anim == null)
                return@launch
            anim.load()

            /*val cfgShowT = MaterialContainerTransform().apply {
                startView = cfgBtn
                endView = cfgMenu

                addTarget(endView!!)
                setPathMotion(MaterialArcMotion())
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            }
            val cfgHideT = MaterialContainerTransform().apply {
                startView = cfgMenu
                endView = cfgBtn

                addTarget(endView!!)
                createAnimator(root, TransitionValues(cfgMenu), TransitionValues(cfgBtn))
                setPathMotion(MaterialArcMotion())
                scrimColor = Color.TRANSPARENT
                fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            }

            cfgBtn.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    TransitionManager.beginDelayedTransition(root, cfgShowT)
                    cfgBtn.visibility = View.GONE
                    cfgMenu.visibility = View.VISIBLE
                }
            })
            cfgHideBtn.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    TransitionManager.beginDelayedTransition(root, cfgHideT)
                    cfgBtn.visibility = View.VISIBLE
                    cfgMenu.visibility = View.GONE
                }
            })*/
            refreshAdapter(anim)

            val viewer = AnimationEditView(this@MaAnimEditor, anim, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true)).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            viewer.id = R.id.animationView
            //layout.addView(viewer)

            //val addl = findViewById<Button>(R.id.maanimpadd)
            //addl.setOnClickListener {
            //    //anim.addMMline(viewer.anim.sele + 1, 0)
            //    refreshAdapter(anim)
            //    anim.unSave("maanim add line")
            //    viewer.animationChanged()
            //}
            //val impr = findViewById<Button>(R.id.maanimimport)
            //impr.setOnClickListener {
            //    val intent = Intent(Intent.ACTION_GET_CONTENT)
            //    intent.addCategory(Intent.CATEGORY_DEFAULT)
            //    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            //    intent.type = "*/*"

            //    tempFunc = fun(f : MaAnim) {
            //        //anim.mamodel = f //TODO - It's more complex than this
            //        //refreshAdapter(anim)
            //        //anim.unSave("Import mamodel")
            //        //viewer.animationChanged()
            //    }
            //    resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
            //}

            /*val bck = findViewById<Button>(R.id.maanimexit)
            bck.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, AnimationManagement::class.java)
                startActivity(intent)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@MaAnimEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val imgcutBtn = findViewById<Button>(R.id.maan_imgcut_btn)
            imgcutBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, ImgCutEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.maan_mamodel_btn)
            maanimBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaAnimEditor, MaModelEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }

            StaticStore.setAppear(cfgBtn, layout)*/
        }
    }

    fun refreshAdapter(anim : AnimCE) {
        //val list = findViewById<ListView>(R.id.maanimvalList) //TODO - It's more complex
        //list.adapter = MaModelListAdapter(this, anim)
    }
}