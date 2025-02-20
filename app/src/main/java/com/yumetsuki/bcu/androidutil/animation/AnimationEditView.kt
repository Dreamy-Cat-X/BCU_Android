package com.yumetsuki.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import com.yumetsuki.bcu.MaModelEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.fakeandroid.CVGraphics
import com.yumetsuki.bcu.androidutil.supports.PP
import common.CommonStatic
import common.pack.Source
import common.system.P
import common.util.anim.AnimCE
import common.util.anim.AnimU
import common.util.anim.EAnimI
import common.util.anim.EAnimS

@SuppressLint("ViewConstructor")
class AnimationEditView(private val context : Context, private val data : AnimCE, night : Boolean, axis : Boolean) : View(context) {

    @JvmField
    var anim: EAnimI
    var type: Array<AnimU.UType> = when (data.id.base) {
        Source.BasePath.ANIM -> AnimU.TYPEDEF
        Source.BasePath.BG -> AnimU.BGEFFECT
        else -> AnimU.SOUL
    }
    var aind: Int = AnimU.WALK

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
            data.getEAnim(type[aind])
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
            data.getEAnim(type[aind])
        anim.sele = s
        invalidate()
    }
}