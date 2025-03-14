package com.yumetsuki.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.MaAnimEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.AnimationEditView
import com.yumetsuki.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.Part

class PartListAdapter(private val activity: MaAnimEditor, private val a : AnimCE, private val p : Part) : RecyclerView.Adapter<PartListAdapter.ViewHolder>() {

    companion object {
        val eases = arrayOf("0 - Linear", "1 - Instant", "2 - Exponential", "3 - Polynomial", "4 - Sinusoidal")
    }
    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val ifr: WatcherEditText = row.findViewById(R.id.mapart_frame)
        val idat: WatcherEditText = row.findViewById(R.id.mapart_mod)
        val iea: Spinner = row.findViewById(R.id.mapart_ease)
        val ipa: WatcherEditText = row.findViewById(R.id.mapart_param)
        val del: FloatingActionButton = row.findViewById(R.id.mapart_delete)

        fun setData(ma : IntArray) {
            ifr.text = SpannableStringBuilder(ma[0].toString())
            idat.text = SpannableStringBuilder(ma[1].toString())
            iea.setSelection(ma[2])
            ipa.text = SpannableStringBuilder(ma[3].toString())

            ipa.visibility = if (ma[2] == 2 || ma[2] == 4)
                View.VISIBLE
            else
                View.GONE
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(activity).inflate(R.layout.maanim_part_list_layout, viewGroup, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return p.n
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val position = holder.bindingAdapterPosition
        val pa = p.moves[position]
        holder.iea.setPopupBackgroundResource(R.drawable.spinner_popup)
        holder.iea.adapter = ArrayAdapter(activity, R.layout.spinneradapter, eases)
        holder.setData(pa)

        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ifr.setWatcher {
            if (!holder.ifr.hasFocus())
                return@setWatcher
            pa[0] = CommonStatic.parseIntN(holder.ifr.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position frame")
            voo.animationChanged()
        }
        holder.idat.setWatcher {
            if (!holder.idat.hasFocus())
                return@setWatcher
            pa[1] = CommonStatic.parseIntN(holder.idat.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position effect")
            voo.animationChanged()
        }
        holder.iea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, position: Int, id: Long) {
                if (pa[2] == position)
                    return
                pa[2] = position
                p.check(a)
                activity.unSave(a,"maanim change part move $position easing")
                holder.ipa.visibility = if (pa[2] == 2 || pa[2] == 4)
                    View.VISIBLE
                else
                    View.GONE
                voo.animationChanged()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        holder.ipa.setWatcher {
            if (!holder.ipa.hasFocus())
                return@setWatcher
            pa[3] = CommonStatic.parseIntN(holder.ipa.text.toString())
            p.check(a)
            activity.unSave(a,"maanim change part move $position effect")
            voo.animationChanged()
        }
        holder.del.setOnClickListener {
            val manim = activity.getAnim(a)
            val data: Array<Part?> = manim.parts
            data[position] = null
            manim.parts = arrayOfNulls(--manim.n)
            var ind = 0
            for (datum in data)
                if (datum != null)
                    manim.parts[ind++] = datum
            manim.validate()
            activity.unSave(a,"maanim remove part")
            notifyItemRemoved(position)
        }
    }
}