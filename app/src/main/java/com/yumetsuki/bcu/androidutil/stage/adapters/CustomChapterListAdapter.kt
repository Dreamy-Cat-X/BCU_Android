package com.yumetsuki.bcu.androidutil.stage.adapters

import android.app.Activity
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.google.android.material.textfield.TextInputEditText
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.pack.PackData.UserPack
import common.util.stage.StageMap

class CustomChapterListAdapter(private val pack : UserPack, ctx: Activity) : StableArrayAdapter<StageMap>(ctx, R.layout.cus_chapter_info_layout, pack.mc.maps.list) {

    internal class ViewHolder(row: View) {
        var chName: TextInputEditText = row.findViewById(R.id.cuschapter_name)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.cus_chapter_info_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }
        val subchapter = pack.mc.maps[position]

        holder.chName.text = SpannableStringBuilder(subchapter.names.toString())
        holder.chName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE || subchapter.names.toString() == holder.chName.text.toString())
                return@setOnEditorActionListener false
            subchapter.names.put(holder.chName.text.toString())
            false
        }

        return row
    }
}