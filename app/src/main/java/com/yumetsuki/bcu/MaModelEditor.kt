package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
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
import com.yumetsuki.bcu.androidutil.supports.DynamicListView
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonDecoder
import common.io.json.JsonEncoder
import common.pack.Source.ResourceLocation
import common.pack.UserProfile
import common.system.P
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.MaModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.cos
import kotlin.math.sin


@SuppressLint("ClickableViewAccessibility")
class MaModelEditor : AppCompatActivity() {

    companion object {
        var tempFunc : ((input: MaModel) -> Unit)? = null
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
                            val imc = MaModel.newIns(FDFile(fl))
                            tempFunc?.invoke(imc)
                        }
                    }
                }
                cursor.close()
            }
        }
    }
    var movePivot = false

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
        setContentView(R.layout.activity_mamodel_editor)

        val result = intent
        val extra = result.extras ?: return

        lifecycleScope.launch {
            val root = findViewById<ConstraintLayout>(R.id.mamodelroot)
            val layout = findViewById<LinearLayout>(R.id.mamodellayout)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.mamodelCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.mamodelMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.mamodelCfgHide)

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

            val viewer = AnimationEditView(this@MaModelEditor, anim, !shared.getBoolean("theme", false), shared.getBoolean("Axis", true)).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            viewer.id = R.id.animationView
            val scaleListener = ScaleListener(viewer, anim)
            val detector = ScaleGestureDetector(this@MaModelEditor, scaleListener)
            viewer.setOnTouchListener(object : OnTouchListener {
                var preid = -1
                var spriteSelected = false
                var partMoved = false
                var preX = 0f
                var preY = 0f

                override fun onTouch(v: View, event: MotionEvent): Boolean {
                    detector.onTouchEvent(event)
                    if (preid == -1)
                        preid = event.getPointerId(0)

                    val id = event.getPointerId(0)

                    val x = event.x
                    val y = event.y

                    if (event.action == MotionEvent.ACTION_DOWN) {
                        scaleListener.updateScale = true
                        spriteSelected = false
                        /*if (viewer.anim.sele != -1) {
                            val mo = anim.mamodel.parts[viewer.anim.sele]
                            val sx = -viewer.pos.x + (cut[0] * viewer.size - 1)
                            val sy = -viewer.pos.y + (cut[1] * viewer.size - 1)
                            val sw = sx + (cut[2] * viewer.size + 2)
                            val sh = sy + (cut[3] * viewer.size + 2)
                            if (!(x in sx..sw && y in sy..sh))
                                viewer.anim.sele = -1
                        } else {
                            val mo = anim.mamodel
                            for (i in 0 until mo.n) {
                                val cut = mo.parts[i]
                                val sx = -viewer.pos.x + (cut[0] * viewer.size - 1)
                                val sy = -viewer.pos.y + (cut[1] * viewer.size - 1)
                                val sw = sx + (cut[2] * viewer.size + 2)
                                val sh = sy + (cut[3] * viewer.size + 2)

                                if (x in sx..sw && y in sy..sh) {
                                    viewer.anim.sele = i
                                    spriteSelected = true
                                    viewer.invalidate()
                                    break
                                }
                            }
                        }*/
                    } else if (event.action == MotionEvent.ACTION_MOVE) {
                        if (event.pointerCount == 1 && id == preid) {
                            var dx = x - preX
                            var dy = y - preY

                            if (viewer.anim.sele == -1) {
                                viewer.pos.x += dx
                                viewer.pos.y += dy
                                if (dx != 0f || dy != 0f)
                                    viewer.invalidate()
                            } else {
                                val mo = anim.mamodel
                                val scale: P = realScale(mo.parts, mo.parts[viewer.anim.sele], !movePivot)
                                dx /= viewer.size / scale.x
                                dy /= viewer.size / scale.y

                                val angle: Double = getAngle(mo.parts, mo.parts[viewer.anim.sele], !movePivot) / 1800.0 * Math.PI
                                val sin = sin(angle)
                                val cos = cos(angle)
                                if (!movePivot || viewer.anim.sele == 0) {
                                    dx *= -1
                                    dy *= -1
                                }
                                partMoved = partMoved || dx.toInt() != 0 || dy.toInt() != 0
                                anim.mamodel.parts[viewer.anim.sele][if (movePivot || viewer.anim.sele == 0) 6 else 4] -= ((dx * cos) + (dy * sin)).toInt()
                                anim.mamodel.parts[viewer.anim.sele][if (movePivot || viewer.anim.sele == 0) 7 else 5] -= ((dy * cos) + (dx * sin)).toInt()
                                if (dx != 0f || dy != 0f) {
                                    partMoved(anim.mamodel.parts[viewer.anim.sele], viewer.anim.sele)
                                    viewer.animationChanged()
                                }
                            }
                        }
                    } else if (event.action == MotionEvent.ACTION_UP) {
                        if (partMoved) {
                            partMoved = false
                            unSave(anim,"mamodel move part ${viewer.anim.sele}")
                        }/* else if (!spriteSelected) {
                            var selected = -1
                            val ic = anim.imgcut
                            for (i in 0 until ic.n) {
                                if (sele == i)
                                    continue
                                val cut = ic.cuts[i]
                                val sx = -pos.x + (cut[0] * zoom - 1)
                                val sy = -pos.y + (cut[1] * zoom - 1)
                                val sw = sx + (cut[2] * zoom + 2)
                                val sh = sy + (cut[3] * zoom + 2)

                                if (x in sx..sw && y in sy..sh) {
                                    selected = i
                                    break
                                }
                            }
                            sele = selected
                            limit()
                        }*/else if (viewer.anim.sele != -1 && scaleListener.scaled())
                            unSave(anim,"mamodel scale part ${viewer.anim.sele}")
                    }

                    preX = x
                    preY = y

                    preid = id

                    return true
                }
            })
            layout.addView(viewer)

            val addl = findViewById<Button>(R.id.mamodelpadd)
            addl.setOnClickListener {
                anim.addMMline(viewer.anim.sele + 1, 0)
                refreshAdapter(anim)
                unSave(anim,"initial")
                viewer.animationChanged()
            }
            val impr = findViewById<Button>(R.id.mamodelimport)
            impr.setOnClickListener {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                intent.type = "*/*"

                tempFunc = fun(f : MaModel) {
                    anim.mamodel = f
                    refreshAdapter(anim)
                    unSave(anim,"Import mamodel")
                    viewer.animationChanged()
                }
                resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
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
                viewer.animationChanged()
            }
            redo.setOnClickListener {
                anim.redo()
                redo.visibility = if (anim.getRedo() == "nothing")
                    View.GONE
                else
                    View.VISIBLE
                undo.visibility = View.VISIBLE
                refreshAdapter(anim)
                viewer.animationChanged()
            }
            undo.visibility = if (anim.undo == "initial")
                View.GONE
            else
                View.VISIBLE
            redo.visibility = if (anim.getRedo() == "nothing")
                View.GONE
            else
                View.VISIBLE

            val bck = findViewById<Button>(R.id.mamodelexit)
            bck.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaModelEditor, AnimationManagement::class.java)
                startActivity(intent)
                finish()
            }
            onBackPressedDispatcher.addCallback(this@MaModelEditor, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    bck.performClick()
                }
            })

            val imgcutBtn = findViewById<Button>(R.id.mamo_imgcut_btn)
            imgcutBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaModelEditor, ImgCutEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }
            val maanimBtn = findViewById<Button>(R.id.mamo_maanim_btn)
            maanimBtn.setOnClickListener {
                anim.save()
                val intent = Intent(this@MaModelEditor, MaAnimEditor::class.java)
                intent.putExtra("Data", JsonEncoder.encode(anim.id).toString())

                startActivity(intent)
                finish()
            }

            StaticStore.setAppear(cfgBtn, layout)
        }
    }

    fun partMoved(mo : IntArray, i : Int) {
        (findViewById<DynamicListView>(R.id.mamodelvalList)[i].tag as MaModelListAdapter.ViewHolder).setData(mo)
    }

    fun refreshAdapter(anim : AnimCE) {
        val list = findViewById<DynamicListView>(R.id.mamodelvalList)
        list.adapter = MaModelListAdapter(this, anim)
        list.setSwapListener { from, to ->
            val s = anim.mamodel.strs0
            val tempe = s[from]
            s[from] = s[to]
            s[to] = tempe

            val p = anim.mamodel.parts
            val temp = p[from]
            p[from] = p[to]
            p[to] = temp
            for (ma in anim.anims)
                for (pt in ma.parts) {
                    if (pt.ints[0] == from)
                        pt.ints[0] = to
                    else if (pt.ints[0] == to)
                        pt.ints[0] = from
                }
            unSave(anim,"mamodel sort")
        }
    }

    private fun realScale(parts : Array<IntArray>, part: IntArray, ignoreFirst: Boolean): P {
        val scale = if (ignoreFirst)
            P(1f, 1f)
        else
            P(part[8] / 1000f, part[9] / 1000f)
        if (part[0] != -1)
            scale.times(realScale(parts, parts[part[0]], false))
        return scale
    }

    private fun getAngle(parts : Array<IntArray>, part: IntArray, ignoreDef: Boolean): Int {
        var a = if (ignoreDef) 0 else part[10]
        if (part[0] != -1)
            a += getAngle(parts, parts[part[0]], false)
        return a
    }

    fun unSave(a : AnimCE, str : String) {
        a.unSave(str)

        val undo = findViewById<FloatingActionButton>(R.id.anim_Undo)
        val redo = findViewById<FloatingActionButton>(R.id.anim_Redo)
        undo.visibility = View.VISIBLE
        redo.visibility = View.GONE
    }

    inner class ScaleListener(private val cView : AnimationEditView, private val anim : AnimCE) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        val model = cView.context as MaModelEditor
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (cView.anim.sele != -1) {
                val mo = anim.mamodel
                mo.parts[cView.anim.sele][9] = (mo.parts[cView.anim.sele][9] * detector.scaleFactor).toInt()
                mo.parts[cView.anim.sele][10] = (mo.parts[cView.anim.sele][10] * detector.scaleFactor).toInt()
            } else {
                cView.size *= detector.scaleFactor

                val diffX = (realFX - previousX) * (cView.size / previousScale - 1)
                val diffY = (realFY - previousY) * (cView.size / previousScale - 1)
                cView.pos.x = previousX - diffX
                cView.pos.y = previousY - diffY
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                if (cView.anim.sele != -1) {
                    val model = anim.mamodel.parts[cView.anim.sele]
                    realFX = model[9].toFloat()
                    realFY = model[10].toFloat()
                } else {
                    realFX = detector.focusX - cView.width / 2f
                    previousX = cView.pos.x

                    realFY = detector.focusY - cView.height * 2f / 3f
                    previousY = cView.pos.y
                }
                previousScale = cView.size
                updateScale = false
            }
            return super.onScaleBegin(detector)
        }

        fun scaled() : Boolean {
            val model = anim.mamodel.parts[cView.anim.sele]
            return model[9] != realFX.toInt() || model[10] != realFY.toInt()
        }
    }
}