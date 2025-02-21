package com.yumetsuki.bcu.androidutil.animation.adapter

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.MaModelEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.AnimationEditView
import com.yumetsuki.bcu.androidutil.animation.adapter.ImgcutListAdapter.ViewHolder
import com.yumetsuki.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaModel
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaModelListAdapter(private val activity: MaModelEditor, private val a : AnimCE) : StableArrayAdapter<IntArray>(activity, R.layout.mamodel_list_layout, a.mamodel.parts) {

    internal class ViewHolder(row: View) {
        val iid: Button = row.findViewById(R.id.mamodel_id)
        val ipar: EditText = row.findViewById(R.id.mamodel_par)
        val ispr: EditText = row.findViewById(R.id.mamodel_spr)
        val iz: EditText = row.findViewById(R.id.mamodel_z)
        val ix: EditText = row.findViewById(R.id.mamodel_x)
        val iy: EditText = row.findViewById(R.id.mamodel_y)
        val ipx: EditText = row.findViewById(R.id.mamodel_px)
        val ipy: EditText = row.findViewById(R.id.mamodel_py)
        val isx: EditText = row.findViewById(R.id.mamodel_sx)
        val isy: EditText = row.findViewById(R.id.mamodel_sy)
        val irot: EditText = row.findViewById(R.id.mamodel_rot)
        val iopa: EditText = row.findViewById(R.id.mamodel_opa)
        val iglw: EditText = row.findViewById(R.id.mamodel_glw)
        val iname: EditText = row.findViewById(R.id.mamodel_name)
        val del: FloatingActionButton = row.findViewById(R.id.mamodel_part_delete)

        fun setData(ic : IntArray) {
            ipar.text = SpannableStringBuilder(ic[0].toString())
            ispr.text = SpannableStringBuilder(ic[2].toString())
            iz.text = SpannableStringBuilder(ic[3].toString())
            ix.text = SpannableStringBuilder(ic[4].toString())
            iy.text = SpannableStringBuilder(ic[5].toString())
            ipx.text = SpannableStringBuilder(ic[6].toString())
            ipy.text = SpannableStringBuilder(ic[7].toString())
            isx.text = SpannableStringBuilder(ic[8].toString())
            isy.text = SpannableStringBuilder(ic[9].toString())
            irot.text = SpannableStringBuilder(ic[10].toString())
            iopa.text = SpannableStringBuilder(ic[11].toString())
            iglw.text = SpannableStringBuilder(ic[12].toString())
        }
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.mamodel_list_layout, parent, false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val mo = a.mamodel.parts[position]
        holder.iid.text = position.toString()

        holder.setData(mo)
        holder.iname.text = SpannableStringBuilder(a.mamodel.strs0[position])
        holder.iname.hint = a.imgcut.strs[mo[2]]
        holder.iname.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            a.mamodel.strs0[position] = holder.iname.text.toString()
            false
        }

        val voo = activity.findViewById<AnimationEditView>(R.id.animationView)
        holder.ipar.doAfterTextChanged {
            if (!holder.ipar.hasFocus())
                return@doAfterTextChanged
            mo[0] = MathUtil.clip(CommonStatic.parseIntN(holder.ipar.text.toString()), -1, a.mamodel.n - 1)
            a.mamodel.check(a)
            activity.unSave(a,"mamodel change $position Parent")
            voo.animationChanged()
        }
        holder.ispr.doAfterTextChanged {
            if (!holder.ispr.hasFocus())
                return@doAfterTextChanged
            mo[2] = MathUtil.clip(CommonStatic.parseIntN(holder.ispr.text.toString()), 0, a.imgcut.n - 1)
            activity.unSave(a,"mamodel change $position Sprite")
            voo.animationChanged()
        }
        holder.iz.doAfterTextChanged {
            if (!holder.iz.hasFocus())
                return@doAfterTextChanged
            mo[3] = CommonStatic.parseIntN(holder.iz.text.toString())
            activity.unSave(a,"mamodel change $position Z-Order")
            voo.animationChanged()
        }
        holder.ix.doAfterTextChanged {
            if (!holder.ix.hasFocus())
                return@doAfterTextChanged
            mo[4] = CommonStatic.parseIntN(holder.ix.text.toString())
            activity.unSave(a,"imgcut change $position x")
            voo.animationChanged()
        }
        holder.iy.doAfterTextChanged {
            if (!holder.iy.hasFocus())
                return@doAfterTextChanged
            mo[5] = CommonStatic.parseIntN(holder.iy.text.toString())
            activity.unSave(a,"imgcut change $position y")
            voo.animationChanged()
        }
        holder.ipx.doAfterTextChanged {
            if (!holder.ipx.hasFocus())
                return@doAfterTextChanged
            mo[6] = CommonStatic.parseIntN(holder.ipx.text.toString())
            activity.unSave(a,"imgcut change $position pivot x")
            voo.animationChanged()
        }
        holder.ipy.doAfterTextChanged {
            if (!holder.ipy.hasFocus())
                return@doAfterTextChanged
            mo[7] = CommonStatic.parseIntN(holder.ipy.text.toString())
            activity.unSave(a,"imgcut change $position pivot y")
            voo.animationChanged()
        }
        holder.isx.doAfterTextChanged {
            if (!holder.isx.hasFocus())
                return@doAfterTextChanged
            mo[8] = CommonStatic.parseIntN(holder.isx.text.toString())
            activity.unSave(a,"imgcut change $position scale x")
            voo.animationChanged()
        }
        holder.isy.doAfterTextChanged {
            if (!holder.isy.hasFocus())
                return@doAfterTextChanged
            mo[9] = CommonStatic.parseIntN(holder.isy.text.toString())
            activity.unSave(a,"imgcut change $position scale y")
            voo.animationChanged()
        }
        holder.irot.doAfterTextChanged {
            if (!holder.irot.hasFocus())
                return@doAfterTextChanged
            mo[10] = CommonStatic.parseIntN(holder.irot.text.toString())
            activity.unSave(a,"imgcut change $position angle")
            voo.animationChanged()
        }
        holder.iopa.doAfterTextChanged {
            if (!holder.iopa.hasFocus())
                return@doAfterTextChanged
            mo[11] = max(0, CommonStatic.parseIntN(holder.iopa.text.toString()))
            activity.unSave(a,"imgcut change $position opacity")
            voo.animationChanged()
        }
        holder.iglw.doAfterTextChanged {
            if (!holder.iglw.hasFocus())
                return@doAfterTextChanged
            mo[12] = MathUtil.clip(CommonStatic.parseIntN(holder.iglw.text.toString()), -1, 3)
            activity.unSave(a,"imgcut change $position glow")
            voo.animationChanged()
        }
        holder.iid.setOnClickListener {
            voo.anim.sele = if (voo.anim.sele == position) -1 else position
            voo.animationChanged()
        }
        holder.del.setOnClickListener {
            val mm : MaModel = a.mamodel
            val data = mm.parts
            val inds = IntArray(data.size)
            val move = IntArray(--mm.n)
            data[position] = null
            var ind = 0
            for (i in data.indices)
                if (data[i] != null) {
                    move[ind] = i
                    inds[i] = ind
                    ind++
                } else
                    inds[i] = -1
            for (ma in a.anims)
                for (part in ma.parts)
                    if (part.ints[1] == 0)
                        for (ints in part.moves)
                            if (ints[1] > position)
                                ints[1]--

            a.reorderModel(inds)
            mm.reorder(move)
            activity.unSave(a,"mamodel remove line")
            activity.refreshAdapter(a)
            voo.invalidate()
        }
        if (a.mamodel.n == 1)
            holder.del.isEnabled = false
        else
            for (mod in a.mamodel.parts)
                if (mod[0] == position) {
                    holder.del.isEnabled = false
                    break
                }
        if (holder.del.isEnabled)
            for (ma in a.anim.anims) {
                for (part in ma.parts)
                    if (part.ints[0] == position) {
                        holder.del.isEnabled = false
                        break
                    }
                if (!holder.del.isEnabled)
                    break
            }
        return row
    }
}