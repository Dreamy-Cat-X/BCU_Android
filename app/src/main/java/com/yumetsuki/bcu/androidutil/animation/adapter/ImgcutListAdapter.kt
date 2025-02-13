package com.yumetsuki.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.yumetsuki.bcu.ImgCutEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.SpriteView
import common.CommonStatic
import common.util.anim.AnimCE
import kotlin.math.max

class ImgcutListAdapter(private val activity: ImgCutEditor, private val a : AnimCE) : ArrayAdapter<IntArray>(activity, R.layout.imgcut_list_layout, a.imgcut.cuts) {

    private class ViewHolder(row: View) {
        val iid: TextView = row.findViewById(R.id.imgcut_id)
        val ix: EditText = row.findViewById(R.id.imgcut_x)
        val iy: EditText = row.findViewById(R.id.imgcut_y)
        val iw: EditText = row.findViewById(R.id.imgcut_w)
        val ih: EditText = row.findViewById(R.id.imgcut_h)
        val iname: EditText = row.findViewById(R.id.imgcut_name)
        val del: Button = row.findViewById(R.id.imgcut_part_delete)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.imgcut_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val ic = a.imgcut.cuts[position]
        holder.iid.text = position.toString()

        holder.ix.text = SpannableStringBuilder(ic[0].toString())
        holder.iy.text = SpannableStringBuilder(ic[1].toString())
        holder.iw.text = SpannableStringBuilder(ic[2].toString())
        holder.ih.text = SpannableStringBuilder(ic[3].toString())
        holder.iname.text = SpannableStringBuilder(a.imgcut.strs[position])
        holder.iname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            a.imgcut.strs[position] = holder.iname.text.toString()
            false
        }

        val view = activity.findViewById<SpriteView>(R.id.spriteView)
        holder.ix.doAfterTextChanged {
            ic[0] = max(1, CommonStatic.parseIntN(holder.ix.text.toString()))
            a.unSave("imgcut change $position x")
            view.invalidate()
        }
        holder.iy.doAfterTextChanged {
            ic[1] = max(1, CommonStatic.parseIntN(holder.iy.text.toString()))
            a.unSave("imgcut change $position y")
            view.invalidate()
        }
        holder.iw.doAfterTextChanged {
            ic[2] = max(1, CommonStatic.parseIntN(holder.iw.text.toString()))
            a.unSave("imgcut change $position w")
            view.invalidate()
        }
        holder.ih.doAfterTextChanged {
            ic[3] = max(1, CommonStatic.parseIntN(holder.ih.text.toString()))
            a.unSave("imgcut change $position h")
            view.invalidate()
        }
        holder.del.setOnClickListener {
            a.removeICline(position)
            activity.refreshAdapter(a)
        }

        return row
    }
}