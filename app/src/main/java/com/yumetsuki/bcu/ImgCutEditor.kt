package com.yumetsuki.bcu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
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
import com.google.android.material.color.utilities.Variant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.yumetsuki.bcu.AnimationManagement.Companion
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.animation.AnimationCView
import com.yumetsuki.bcu.androidutil.animation.SpriteView
import com.yumetsuki.bcu.androidutil.animation.adapter.ImgcutListAdapter
import com.yumetsuki.bcu.androidutil.fakeandroid.FIBM
import com.yumetsuki.bcu.androidutil.io.AContext
import com.yumetsuki.bcu.androidutil.io.DefineItf
import com.yumetsuki.bcu.androidutil.supports.LeakCanaryManager
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.system.P
import common.system.files.FDFile
import common.util.anim.AnimCE
import common.util.anim.ImgCut
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
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
            val bck = findViewById<Button>(R.id.imgcutexit)

            val cfgBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgDisplay)
            val cfgMenu = findViewById<RelativeLayout>(R.id.imgCutMenu)
            val cfgHideBtn = findViewById<FloatingActionButton>(R.id.imgcutCfgHide)

            StaticStore.setDisappear(cfgBtn, layout)

            val anim = AnimCE.map()[extra.getString("Data")] ?: return@launch
            var ic = anim.imgcut

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
            }
            viewer.id = R.id.spriteView
            layout.addView(viewer)
            viewer.calculateSize(true)

            val scaleListener = ScaleListener(viewer)
            val detector = ScaleGestureDetector(this@ImgCutEditor, scaleListener)
            viewer.setOnTouchListener(object : View.OnTouchListener {
                var preid = -1

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
                    }

                    if (event.action == MotionEvent.ACTION_MOVE) {
                        if (event.pointerCount == 1 && id == preid) {
                            val dx = x - preX
                            val dy = y - preY

                            viewer.pos.x += dx
                            viewer.pos.y += dy
                            viewer.limit()
                        }
                    }

                    preX = x
                    preY = y

                    preid = id

                    return true
                }
            })

            val addl = findViewById<Button>(R.id.imgcutpadd)
            val impr = findViewById<Button>(R.id.imgcutimport)
            val spri = findViewById<Button>(R.id.imgcutsprimp)
            addl.setOnClickListener {
                ic.addLine(-1)
                refreshAdapter(anim)
                anim.unSave("imgcut add line")
                viewer.invalidate()
            }
            impr.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f is ImgCut) {
                        anim.imgcut = f
                        ic = f
                        refreshAdapter(anim)
                        anim.unSave("Import imgcut")
                        viewer.invalidate()
                    }
                })
            }
            spri.setOnClickListener {
                getImport(fun(f : Any) {
                    if (f is Bitmap) {
                        anim.num = FIBM(f)
                        anim.saveImg()
                        anim.reloImg()
                        viewer.invalidate()
                    }
                })
            }

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

            StaticStore.setAppear(cfgBtn, layout)
        }
    }

    fun refreshAdapter(anim : AnimCE) {
        val list = findViewById<ListView>(R.id.imgcutvalList)
        list.adapter = ImgcutListAdapter(this, anim)
    }

    private fun getImport(func: (f: Any) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "*/*"

        tempFunc = func
        resultLauncher.launch(Intent.createChooser(intent, "Choose Directory"))
    }

    inner class ScaleListener(private val cView: SpriteView) : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        var updateScale = false

        private var realFX = 0f
        private var previousX = 0f

        private var realFY = 0f
        private var previousY = 0f

        private var previousScale = 0f

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            cView.zoom *= detector.scaleFactor

            val diffX = (realFX - previousX) * (cView.zoom / previousScale - 1)
            val diffY = (realFY - previousY) * (cView.zoom / previousScale - 1)

            cView.pos.x = previousX - diffX
            cView.pos.y = previousY - diffY
            cView.limit()

            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (updateScale) {
                realFX = detector.focusX - cView.width / 2f
                previousX = cView.pos.x

                realFY = detector.focusY - cView.height * 2f / 3f
                previousY = cView.pos.y

                previousScale = cView.zoom

                updateScale = false
            }

            return super.onScaleBegin(detector)
        }
    }
}