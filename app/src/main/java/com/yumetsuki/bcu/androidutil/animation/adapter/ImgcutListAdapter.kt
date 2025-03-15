package com.yumetsuki.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.yumetsuki.bcu.ImgCutEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.SpriteView
import com.yumetsuki.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.util.anim.AnimCE
import kotlin.math.max


class ImgcutListAdapter(private val activity: ImgCutEditor, private val a : AnimCE) : RecyclerView.Adapter<ImgcutListAdapter.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val iid: Button = row.findViewById(R.id.imgcut_id)
        val ix: WatcherEditText = row.findViewById(R.id.imgcut_x)
        val iy: WatcherEditText = row.findViewById(R.id.imgcut_y)
        val iw: WatcherEditText = row.findViewById(R.id.imgcut_w)
        val ih: WatcherEditText = row.findViewById(R.id.imgcut_h)
        val iname: EditText = row.findViewById(R.id.imgcut_name)

        fun setData(ic : IntArray) {
            ix.text = SpannableStringBuilder(ic[0].toString())
            iy.text = SpannableStringBuilder(ic[1].toString())
            iw.text = SpannableStringBuilder(ic[2].toString())
            ih.text = SpannableStringBuilder(ic[3].toString())
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.imgcut_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return a.imgcut.n
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val pos = holder.bindingAdapterPosition
        val ic = a.imgcut.cuts[pos]
        holder.iid.text = pos.toString()

        holder.setData(ic)
        holder.iname.text = SpannableStringBuilder(a.imgcut.strs[pos])
        holder.iname.setOnEditorActionListener { _, _, _ ->
            if (a.imgcut.strs[pos] == holder.iname.text.toString())
                return@setOnEditorActionListener false
            a.imgcut.strs[pos] = holder.iname.text.toString()
            false
        }

        val voo = activity.findViewById<SpriteView>(R.id.spriteView)
        holder.ix.setWatcher {
            if (!holder.ix.hasFocus())
                return@setWatcher
            ic[0] = CommonStatic.parseIntN(holder.ix.text.toString())
            activity.unSave(a,"imgcut change $pos x")
            voo.invalidate()
        }
        holder.iy.setWatcher {
            if (!holder.iy.hasFocus())
                return@setWatcher
            ic[1] = CommonStatic.parseIntN(holder.iy.text.toString())
            activity.unSave(a,"imgcut change $pos y")
            voo.invalidate()
        }
        holder.iw.setWatcher {
            if (!holder.iw.hasFocus())
                return@setWatcher
            ic[2] = max(1, CommonStatic.parseIntN(holder.iw.text.toString()))
            activity.unSave(a,"imgcut change $pos w")
            voo.invalidate()
        }
        holder.ih.setWatcher {
            if (!holder.ih.hasFocus())
                return@setWatcher
            ic[3] = max(1, CommonStatic.parseIntN(holder.ih.text.toString()))
            activity.unSave(a,"imgcut change $pos h")
            voo.invalidate()
        }
        holder.iid.setOnClickListener {
            voo.sele = if (voo.sele == pos) -1 else pos
            voo.invalidate()
        }
    }
}