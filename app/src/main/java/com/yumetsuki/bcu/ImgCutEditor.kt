package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import androidx.transition.TransitionValues
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.gson.JsonParser
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.animation.SpriteView
import com.yumetsuki.bcu.androidutil.animation.adapter.ImgcutListAdapter
import com.yumetsuki.bcu.androidutil.fakeandroid.FIBM
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.DynamicListView
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.ImgCut
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException

class ImgCutEditor : AppCompatActivity() {

    companion object {
        var tempFunc : ((input: Any) -> Unit)? = null
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

                    if(name.endsWith(".png") || name.endsWith(".jpg")) {
                        val input = this.contentResolver.openInputStream(path)
                        val img = BitmapFactory.decodeStream(input, null, BitmapFactory.Options())
                        input!!.close()
                        if (img == null)
                            return@registerForActivityResult
                        tempFunc?.invoke(img)
                    } else if(name.endsWith(".txt") || name.endsWith(".imgcut")) {
                        val fl = File(StaticStore.getExternalPack(this), name)
                        if (fl.exists()) {
                            val imc = ImgCut.newIns(FDFile(fl))
                            tempFunc?.invoke(imc)
                        }
                    }
                }
                cursor.close()
            }
        }
    }

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
        } else if (!shared.getBoolean("theme", false))
            setTheme(R.style.AppTheme_night)
        else
            setTheme(R.style.AppTheme_day)

        LeakCanaryManager.initCanary(shared, application)
        DefineItf.check(this)
        AContext.check()
        (CommonStatic.ctx as AContext).updateActivity(this)
        setContentView(R.layout.activity_imgcut_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val root = findViewById<ConstraintLayout>(R.id.imgcutroot)
            val layout = findViewById<LinearLayout>(R.id.imgcutlayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.imgCutMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgHide)

            StaticStore.setDisappear(cfgBtn, layout)

            val res = JsonDecoder.decode(JsonParser.parseString(extra.getString("Data")), ResourceLocation::class.java) ?: return@launch
            val anim = if (res.pack == ResourceLocation.LOCAL)
                AnimCE.map()[res.id]
            else
                UserProfile.getUserPack(res.pack)?.source?.loadAnimation(res.id, res.base) as AnimCE?
            if (anim == null)
                return@launch
            anim.load()

            val cfgShowT = MaterialContainerTransform().apply {
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
            })
            refreshAdapter(anim)

            val viewer = SpriteView(this@ImgCutEditor, anim).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                postMove = fun(sav : String) {
                    if (sav.isBlank()) {
                        val list = this@ImgCutEditor.findViewById<DynamicListView>(R.id.imgcutvalList)
                        if (list.size > sele)
                            (list[sele].tag as ImgcutListAdapter.ViewHolder).setData(anim.imgcut.cuts[sele])
                    } else
                        unSave(anim, sav)
                }
            }
            viewer.id = R.id.spriteView
            layout.addView(viewer)

            val addl = findViewById<Button>(R.id.imgcutpadd)
            val impr = findViewById<Button>(R.id.imgcutimport)
            val spri = findViewById<Button>(R.id.imgcutsprimp)
            addl.setOnClickListener {
                anim.imgcut.addLine(-1)
                refreshAdapter(anim)
                unSave(anim, "imgcut add line")
                viewer.invalidate()
            }
            impr.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f !is ImgCut)
                        return
                    anim.imgcut = f
                    refreshAdapter(anim)
                    unSave(anim,"Import imgcut")
                    viewer.invalidate()
                })
            }
            spri.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f !is Bitmap)
                        return
                    anim.num = FIBM(f)
                    anim.saveImg()
                    anim.reloImg()
                    viewer.calculateSize(true)
                })
            }

            val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
            val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
            undo.setOnClickListener {
                anim.undo()
                undo.visibility = if (anim.undo == "initial")
                    View.GONE
                else
                    View.VISIBLE
                redo.visibility = View.VISIBLE
                refreshAdapter(anim)
                viewer.invalidate()
            }
            redo.setOnClickListener {
                anim.redo()
                redo.visibility = if (anim.getRedo() == "nothing")
                    View.GONE
                else
                    View.VISIBLE
                undo.visibility = View.VISIBLE
                refreshAdapter(anim)
                viewer.invalidate()
            }
            undo.visibility = if (anim.undo == "initial")
                View.GONE
            else
                View.VISIBLE
            redo.visibility = if (anim.getRedo() == "nothing")
                View.GONE
            else
                View.VISIBLE

            val bck = findViewById<Button>(R.id.imgcutexit)
            bck.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, AnimationManagement::class.java)
                startActivity(intent)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@ImgCutEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val mamodelBtn = findViewById<Button>(R.id.imgc_mamodel_btn)
            mamodelBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, MaModelEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.imgc_maanim_btn)
            maanimBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@ImgCutEditor, MaAnimEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }

            StaticStore.setAppear(cfgBtn, layout)
        }
    }

    fun refreshAdapter(anim : AnimCE) {
        val list = findViewById<DynamicListView>(R.id.imgcutvalList)
        list.adapter = ImgcutListAdapter(this, anim)
        list.setSwapListener { from, to ->
            val s = anim.imgcut.strs
            val tempe = s[from]
            s[from] = s[to]
            s[to] = tempe

            val c = anim.imgcut.cuts
            val temp = c[from]
            c[from] = c[to]
            c[to] = temp
            for (p in anim.mamodel.parts) {
                if (p[2] == from)
                    p[2] = to
                else if (p[2] == to)
                    p[2] = from
            }
            for (ma in anim.anims)
                for (pt in ma.parts)
                    if (pt.ints[1] == 2)
                        for (mov in pt.moves) {
                            if (mov[1] == from)
                                mov[1] = to
                            else if (mov[1] == to)
                                mov[1] = from
                        }
            unSave(anim,"imgcut sort")
            //findViewById<SpriteView>(R.id.spriteView).sele = -1
        }
    }

    private fun getImport(func: (f: Any) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "*/*"

        tempFunc = func
        resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
    }

    fun unSave(a : AnimCE, str : String) {
        a.unSave(str)

        val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
        val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
        undo.visibility = View.VISIBLE
        redo.visibility = View.GONE
    }
}