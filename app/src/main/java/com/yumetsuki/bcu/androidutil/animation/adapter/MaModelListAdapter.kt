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
import com.yumetsuki.bcu.MaModelEditor
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.animation.AnimationEditView
import com.yumetsuki.bcu.androidutil.animation.adapter.ImgcutListAdapter.ViewHolder
import common.CommonStatic
import common.util.anim.AnimCE
import common.util.anim.MaModel
import org.jcodec.common.tools.MathUtil
import kotlin.math.max


class MaModelListAdapter(private val activity: MaModelEditor, private val a : AnimCE) : ArrayAdapter<IntArray>(activity, R.layout.mamodel_list_layout, a.mamodel.parts) {

    private class ViewHolder(row: View) {
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
        val del: Button = row.findViewById(R.id.mamodel_part_delete)

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
            var ind = MathUtil.clip(CommonStatic.parseIntN(holder.ispr.text.toString()), -1, a.mamodel.n - 1)
            if (ind == position || unviableParent(position, ind, a.mamodel.parts))
                ind = -1
            mo[0] = ind
            a.unSave("mamodel change $position Parent")
            voo.animationChanged()
        }
        holder.ispr.doAfterTextChanged {
            mo[2] = MathUtil.clip(CommonStatic.parseIntN(holder.ispr.text.toString()), 0, a.imgcut.n - 1)
            a.unSave("mamodel change $position Sprite")
            voo.animationChanged()
        }
        holder.iz.doAfterTextChanged {
            mo[3] = CommonStatic.parseIntN(holder.iz.text.toString())
            a.unSave("mamodel change $position Z-Order")
            voo.animationChanged()
        }
        holder.ix.doAfterTextChanged {
            mo[4] = CommonStatic.parseIntN(holder.ix.text.toString())
            a.unSave("imgcut change $position x")
            voo.animationChanged()
        }
        holder.iy.doAfterTextChanged {
            mo[5] = CommonStatic.parseIntN(holder.iy.text.toString())
            a.unSave("imgcut change $position y")
            voo.animationChanged()
        }
        holder.ipx.doAfterTextChanged {
            mo[6] = CommonStatic.parseIntN(holder.ipx.text.toString())
            a.unSave("imgcut change $position pivot x")
            voo.animationChanged()
        }
        holder.ipy.doAfterTextChanged {
            mo[7] = CommonStatic.parseIntN(holder.ipy.text.toString())
            a.unSave("imgcut change $position pivot y")
            voo.animationChanged()
        }
        holder.isx.doAfterTextChanged {
            mo[8] = CommonStatic.parseIntN(holder.isx.text.toString())
            a.unSave("imgcut change $position scale x")
            voo.animationChanged()
        }
        holder.isy.doAfterTextChanged {
            mo[9] = CommonStatic.parseIntN(holder.isy.text.toString())
            a.unSave("imgcut change $position scale y")
            voo.animationChanged()
        }
        holder.irot.doAfterTextChanged {
            mo[10] = CommonStatic.parseIntN(holder.irot.text.toString())
            a.unSave("imgcut change $position angle")
            voo.animationChanged()
        }
        holder.iopa.doAfterTextChanged {
            mo[11] = max(0, CommonStatic.parseIntN(holder.iopa.text.toString()))
            a.unSave("imgcut change $position opacity")
            voo.animationChanged()
        }
        holder.iglw.doAfterTextChanged {
            mo[12] = MathUtil.clip(CommonStatic.parseIntN(holder.iglw.text.toString()), -1, 3)
            a.unSave("imgcut change $position glow")
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
            a.unSave("mamodel remove line")
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

    private fun unviableParent(to : Int, part : Int, parts : Array<IntArray>) : Boolean {
        if (to == -1 || part == -1)
            return false
        if (parts[part][0] == to)
            return true
        return unviableParent(to, parts[part][0], parts)
    }
}