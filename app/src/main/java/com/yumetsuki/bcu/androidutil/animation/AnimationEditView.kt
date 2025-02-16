package com.yumetsuki.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.yumetsuki.bcu.ImgCutEditor
import com.yumetsuki.bcu.MaModelEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.fakeandroid.CVGraphics
import com.yumetsuki.bcu.androidutil.supports.PP
import common.CommonStatic
import common.system.P
import common.util.anim.AnimCE
import common.util.anim.AnimU
import common.util.anim.EAnimI
import common.util.anim.EAnimS

@SuppressLint("ViewConstructor")
class AnimationEditView(private val context : Context, private val data : AnimCE, night : Boolean, axis : Boolean) : View(context) {

    @JvmField
    var anim: EAnimI

    var started = false
    var fps: Long = 0
    var size = 1f
    var pos = PP(0f, 0f)
    private var p2: P

    private val backgroundPaint = Paint()
    private val colorPaint = Paint()
    private val bitmapPaint = Paint()
    private val range = Paint()
    private val cv: CVGraphics

    init {
        anim = if (context is MaModelEditor)
            EAnimS(data, data.mamodel)
        else
            data.getEAnim(AnimU.TYPEDEF[AnimU.WALK])
        StaticStore.play = context !is MaModelEditor

        CommonStatic.getConfig().ref = axis

        if(CommonStatic.getConfig().viewerColor != -1) {
            backgroundPaint.color = CommonStatic.getConfig().viewerColor
            range.color = 0xFFFFFF - CommonStatic.getConfig().viewerColor
        } else {
            if (night) {
                backgroundPaint.color = 0x363636
            } else
                backgroundPaint.color = Color.WHITE

            range.color = StaticStore.getAttributeColor(context, R.attr.TextPrimary)
        }
        colorPaint.isFilterBitmap = true
        p2 = P(width.toFloat() / 2, height.toFloat() * 2f / 3f)

        cv = CVGraphics(Canvas(), colorPaint, bitmapPaint, night)

        setOnTouchListener(object : OnTouchListener {
            var preid = -1
            var spriteSelected = false
            var spriteMoved = false
            var preX = 0f
            var preY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                //detector.onTouchEvent(event)
                if (preid == -1)
                    preid = event.getPointerId(0)

                val id = event.getPointerId(0)

                val x = event.x
                val y = event.y

                if (event.action == MotionEvent.ACTION_DOWN) {
                    /*scaleListener.updateScale = true
                    spriteSelected = false
                    if (sele != -1) {
                        val cut = anim.imgcut.cuts[sele]
                        val sx = -pos.x + (cut[0] * zoom - 1)
                        val sy = -pos.y + (cut[1] * zoom - 1)
                        val sw = sx + (cut[2] * zoom + 2)
                        val sh = sy + (cut[3] * zoom + 2)
                        if (!(x in sx..sw && y in sy..sh))
                            sele = -1
                    } else {
                        val ic = anim.imgcut
                        for (i in 0 until ic.n) {
                            val cut = ic.cuts[i]
                            val sx = -pos.x + (cut[0] * zoom - 1)
                            val sy = -pos.y + (cut[1] * zoom - 1)
                            val sw = sx + (cut[2] * zoom + 2)
                            val sh = sy + (cut[3] * zoom + 2)

                            if (x in sx..sw && y in sy..sh) {
                                sele = i
                                spriteSelected = true
                                limit()
                                break
                            }
                        }
                    }*/
                } else if (event.action == MotionEvent.ACTION_MOVE) {
                    if (event.pointerCount == 1 && id == preid) {
                        var dx = x - preX
                        var dy = y - preY

                        if (anim.sele == -1) {
                            pos.x += dx
                            pos.y += dy
                            if (dx != 0f || dy != 0f)
                                invalidate()
                        } else {
                            dx /= size //TODO: This needs to take scale and angle into account too
                            dy /= size
                            spriteMoved = spriteMoved || dx.toInt() != 0 || dy.toInt() != 0
                            data.mamodel.parts[anim.sele][4] += dx.toInt()
                            data.mamodel.parts[anim.sele][5] += dy.toInt()
                            if (dx != 0f || dy != 0f) {
                                //if (context is MaModelEditor)
                                //    (context as MaModelEditor).spriteMoved(anim.mamodel.parts[anim.sele], sele)
                                animationChanged()
                            }
                        }
                    }
                } else if (event.action == MotionEvent.ACTION_UP) {
                    if (spriteMoved) {
                        spriteMoved = false
                        data.unSave("mamodel move part ${anim.sele}")
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
                    } else if (sele != -1 && scaleListener.scaled())
                        anim.unSave("imgcut scale part $sele")*/
                }

                preX = x
                preY = y

                preid = id

                return true
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        started = true
    }

    override fun onDraw(canvas: Canvas) {
        p2 = P.newP(width / 2f + pos.x, height * 2f / 3 + pos.y)
        cv.setCanvas(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        cv.setColor(range.color)
        anim.draw(cv, p2, size)
        if (StaticStore.play) {
            anim.update(true)
            StaticStore.frame += CommonStatic.fltFpsDiv(1f)
        }
        P.delete(p2)
    }

    fun animationChanged() {
        val s = anim.sele
        anim = if (context is MaModelEditor)
            EAnimS(data, data.mamodel)
        else
            data.getEAnim(AnimU.TYPEDEF[AnimU.WALK])
        anim.sele = s
        invalidate()
    }
}