package com.yumetsuki.bcu.androidutil.animation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.view.View
import com.yumetsuki.bcu.androidutil.supports.PP
import common.util.anim.AnimCE
import kotlin.math.max
import kotlin.math.min

@SuppressLint("ViewConstructor")
class SpriteView(context: Context, val anim : AnimCE) : View(context) {

    var sele : Int = -1
    private var initzoom : Float = 1f
    var zoom : Float = 1f
    var pos : PP = PP(0f, 0f)
    private var cwhite : Boolean = true

    val paint : Paint = Paint()
    val bitPaint : Paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
    }
    val m: Matrix = Matrix()

    override fun onDraw(c: Canvas) {
        calculateSize(false)
        val spr : Bitmap = anim.num.bimg() as Bitmap

        val aw: Int = spr.getWidth()
        val ah: Int = spr.getHeight()
        val rw: Int = (zoom * aw).toInt()
        val rh: Int = (zoom * ah).toInt()
        val bw : Float = if (rw % 20 != 0) 20f * (1 + rw / 20) else rw.toFloat()
        val bh : Float = if (rh % 20 != 0) 20f * (1 + rh / 20) else rh.toFloat()

        m.reset()
        c.setMatrix(m)

        paint.style = Paint.Style.FILL
        paint.color = if (cwhite) Color.WHITE else Color.DKGRAY
        c.drawRect(0f, 0f, bw, bh, paint)

        paint.color = Color.LTGRAY
        var wi = 0f
        while (wi < rw) {
            var hi = (wi / 20).toInt() % 2 * 20f
            while (hi < rh) {
                c.drawRect(wi, hi, wi + 20f, hi + 20f, paint)
                hi += 40
            }
            wi += 20
        }

        m.preTranslate(-pos.x, -pos.y)
        m.preScale(zoom, zoom)
        c.drawBitmap(spr, m, bitPaint)

        val ic = anim.imgcut
        for (i in 0 until ic.n) {
            val cut = ic.cuts[i]
            val sx = -pos.x + (cut[0] * zoom - 1)
            val sy = -pos.y + (cut[1] * zoom - 1)
            val sw = sx + (cut[2] * zoom + 2)
            val sh = sy + (cut[3] * zoom + 2)
            if (i == sele) {
                paint.color = Color.RED
                paint.style = Paint.Style.FILL
                c.drawRect(sx - 5, sy - 5, sw + 5, 5f, paint)
                c.drawRect(sx - 5, sy, 5f, sh + 5, paint)
                c.drawRect(sx + sw, sy - 5, 5f, sh + 5, paint)
                c.drawRect(sx, sy + sh, sw + 5, 5f, paint)
            } else {
                paint.color = Color.BLACK
                paint.style = Paint.Style.STROKE
                c.drawRect(sx, sy, sw, sh, paint)
            }
        }
    }

    fun limit() {
        if (zoom <= initzoom)
            zoom = initzoom

        val spriteW = (anim.num.width * zoom).toInt()
        val spriteH = (anim.num.height * zoom).toInt()

        if (pos.x < 0 || width >= spriteW) {
            pos.x = 0f
        } else if (pos.x + width >= spriteW)
            pos.x = max(0f, (spriteW - width).toFloat())

        if (pos.y < 0 || height >= spriteH) {
            pos.y = 0f
        } else if (pos.y + height >= spriteH)
            pos.y = max(0f, (spriteH - height).toFloat())
        invalidate()
    }

    fun calculateSize(first: Boolean) {
        val img: Bitmap = anim.num.bimg() as Bitmap

        val spriteW: Int = img.getWidth()
        val spriteH: Int = img.getHeight()

        val w = width
        val h = height

        initzoom = min(1f * w / spriteW, 1f * h / spriteH)
        if (first || zoom == 0f)
            zoom = initzoom
    }
}