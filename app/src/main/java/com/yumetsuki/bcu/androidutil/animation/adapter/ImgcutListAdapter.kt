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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.ImgCutEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.SpriteView
import common.CommonStatic
import common.util.anim.AnimCE
import kotlin.math.max


class ImgcutListAdapter(private val activity: ImgCutEditor, private val a : AnimCE) : ArrayAdapter<IntArray>(activity, R.layout.imgcut_list_layout, a.imgcut.cuts) {

    internal class ViewHolder(row: View) {
        val iid: Button = row.findViewById(R.id.imgcut_id)
        val ix: EditText = row.findViewById(R.id.imgcut_x)
        val iy: EditText = row.findViewById(R.id.imgcut_y)
        val iw: EditText = row.findViewById(R.id.imgcut_w)
        val ih: EditText = row.findViewById(R.id.imgcut_h)
        val iname: EditText = row.findViewById(R.id.imgcut_name)
        val del: FloatingActionButton = row.findViewById(R.id.imgcut_part_delete)

        fun setData(ic : IntArray) {
            ix.text = SpannableStringBuilder(ic[0].toString())
            iy.text = SpannableStringBuilder(ic[1].toString())
            iw.text = SpannableStringBuilder(ic[2].toString())
            ih.text = SpannableStringBuilder(ic[3].toString())
        }
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

        holder.setData(ic)
        holder.iname.text = SpannableStringBuilder(a.imgcut.strs[position])
        holder.iname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            a.imgcut.strs[position] = holder.iname.text.toString()
            false
        }

        val voo = activity.findViewById<SpriteView>(R.id.spriteView)
        holder.ix.doAfterTextChanged {
            ic[0] = CommonStatic.parseIntN(holder.ix.text.toString())
            a.unSave("imgcut change $position x")
            voo.invalidate()
        }
        holder.iy.doAfterTextChanged {
            ic[1] = CommonStatic.parseIntN(holder.iy.text.toString())
            a.unSave("imgcut change $position y")
            voo.invalidate()
        }
        holder.iw.doAfterTextChanged {
            ic[2] = max(1, CommonStatic.parseIntN(holder.iw.text.toString()))
            a.unSave("imgcut change $position w")
            voo.invalidate()
        }
        holder.ih.doAfterTextChanged {
            ic[3] = max(1, CommonStatic.parseIntN(holder.ih.text.toString()))
            a.unSave("imgcut change $position h")
            voo.invalidate()
        }
        holder.iid.setOnClickListener {
            voo.sele = if (voo.sele == position) -1 else position
            voo.invalidate()
        }
        holder.del.setOnClickListener {
            a.removeICline(position)
            activity.refreshAdapter(a)
            voo.invalidate()
        }
        for (mod in a.mamodel.parts)
            if (mod[2] == position) {
                holder.del.isEnabled = false
                break
            }
        if (holder.del.isEnabled) {
            for (ma in a.anim.anims) {
                for (part in ma.parts) {
                    if (part.ints[1] == 2)
                        for (mov in part.moves)
                            if (mov[1] == position) {
                                holder.del.isEnabled = false
                                break
                            }
                    if (!holder.del.isEnabled)
                        break
                }
                if (!holder.del.isEnabled)
                    break
            }
        }

        return row
    }
}