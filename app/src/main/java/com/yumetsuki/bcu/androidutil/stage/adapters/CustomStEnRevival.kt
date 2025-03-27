package com.yumetsuki.bcu.androidutil.stage.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableRow
import androidx.recyclerview.widget.RecyclerView
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.supports.WatcherEditText
import common.util.stage.SCDef.Line

class CustomStEnRevival(private val ctx: Activity, private val ent: Line) : RecyclerView.Adapter<CustomStEnRevival.ViewHolder>() {

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val icon = row.findViewById<ImageView>(R.id.strevlisticon)!!
        val info = row.findViewById<ImageButton>(R.id.strevlistinfo)!!
        val type = row.findViewById<Spinner>(R.id.strevlistptype)!!
        val mulh = row.findViewById<WatcherEditText>(R.id.strevlistmultir)!!
        val bgm = row.findViewById<Button>(R.id.strevlistbgm)!!
        val soul = row.findViewById<Spinner>(R.id.strevlistsoul)!!
        val revRow = row.findViewById<TableRow>(R.id.nextRevRow)!!
        val erev = row.findViewById<ImageButton>(R.id.enemlistrev)!!
    }

    var expansions : Int = 0

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return expansions
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        TODO("Not yet implemented")
    }
}