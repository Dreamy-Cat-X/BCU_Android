package com.yumetsuki.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.BattlePrepare
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.MapColc.PackMapColc
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.stage.StageMap
import common.util.stage.info.CustomStageInfo
import common.util.unit.AbEnemy

class CustomStageListAdapter(private val activity: Activity, private val map: StageMap) : StableArrayAdapter<Stage>(activity, R.layout.stage_list_layout, map.list.list) {

    private class ViewHolder constructor(row: View) {
        val name: EditText = row.findViewById(R.id.stagename)
        val icons: FlexboxLayout = row.findViewById(R.id.enemicon)
        val play: Button = row.findViewById(R.id.ch_stagePlay)
        val delete: FloatingActionButton = row.findViewById(R.id.ch_deleteStage)

        val info: TableLayout = row.findViewById(R.id.cusstage_info)
        val expand: ImageButton = row.findViewById(R.id.cusstage_expand)

        val width: EditText = row.findViewById(R.id.ch_stwidth)
        val health: EditText = row.findViewById(R.id.ch_sthealth)
        val maxEne: EditText = row.findViewById(R.id.ch_stmaxEne)
        val dojo: ToggleButton = row.findViewById(R.id.ch_dojo)
        val bossguard: ToggleButton = row.findViewById(R.id.ch_bossguard)
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if(view == null) {
            val inf = LayoutInflater.from(context)
            row = inf.inflate(R.layout.cus_stage_info_layout,parent,false)
            holder = ViewHolder(row)
            row.tag = holder
        } else {
            row = view
            holder = row.tag as ViewHolder
        }

        val st = map.list[position] ?: return row

        holder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

            StaticStore.infoClick = SystemClock.elapsedRealtime()

            if (holder.info.height == 0) {
                holder.info.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = holder.info.measuredHeight
                val anim = ValueAnimator.ofInt(0, height)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.info.layoutParams
                    layout.height = `val`
                    holder.info.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_more_black_24dp))
            } else {
                holder.info.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = holder.info.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.info.layoutParams
                    layout.height = `val`
                    holder.info.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_expand_less_black_24dp))
            }
        })

        holder.name.text = SpannableStringBuilder(MultiLangCont.get(st) ?: st.names.toString())
        if(holder.name.text.isBlank())
            holder.name.text = SpannableStringBuilder(getStageName(st.id()))
        holder.name.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NONE || st.names.toString() == holder.name.text.toString())
                return@setOnEditorActionListener false
            st.names.put(holder.name.text.toString())
            false
        }
        holder.width.hint = "${activity.getString(R.string.def_stg_length)}: ${st.len}"
        holder.width.setOnEditorActionListener { _, actionId, _ ->
            val wid = CommonStatic.parseIntN(holder.width.text.toString())
            if (wid < 2000 || actionId == EditorInfo.IME_ACTION_NONE || wid == st.len)
                return@setOnEditorActionListener false
            st.len = wid
            false
        }
        holder.health.hint = if (st.trail)
            "${activity.getString(R.string.def_base_health)}: ${st.timeLimit}"
        else
            "${activity.getString(R.string.def_base_health)}: ${st.health}"
        holder.health.setOnEditorActionListener { _, actionId, _ ->
            val wid = CommonStatic.parseIntN(holder.health.text.toString())
            if (wid <= 0 || actionId == EditorInfo.IME_ACTION_NONE || wid == st.health)
                return@setOnEditorActionListener false
            if (st.trail)
                st.timeLimit = wid
            else
                st.health = wid
            false
        }
        holder.maxEne.hint = "${activity.getString(R.string.def_max_enemy)}: ${st.max}"
        holder.maxEne.setOnEditorActionListener { _, actionId, _ ->
            val wid = CommonStatic.parseIntN(holder.maxEne.text.toString())
            if (wid <= 0 || actionId == EditorInfo.IME_ACTION_NONE || wid == st.max)
                return@setOnEditorActionListener false
            st.max = wid
            false
        }
        holder.dojo.isChecked = st.trail
        holder.dojo.setOnClickListener {
            st.trail = holder.dojo.isChecked
            holder.health.hint = if (st.trail)
                "${activity.getString(R.string.def_base_health)}: ${st.timeLimit}"
            else
                "${activity.getString(R.string.def_base_health)}: ${st.health}"
        }
        holder.bossguard.isChecked = st.bossGuard
        holder.bossguard.setOnClickListener {
            st.bossGuard = holder.bossguard.isChecked
        }

        holder.icons.removeAllViews()
        val ids = getid(st.data)
        if (ids.isNotEmpty())
            for (i in ids.indices) {
                val icn = getIcon(ids[i])
                val icon = ImageView(activity)
                icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                icon.setImageBitmap(icn)
                icon.setPadding(StaticStore.dptopx(12f, activity), StaticStore.dptopx(4f, activity), 0, StaticStore.dptopx(4f, activity))
                holder.icons.addView(icon)
            }

        holder.delete.setOnClickListener {
            map.list.remove(st)
            if (st.info != null)
                (st.info as CustomStageInfo).destroy(false)
            for (si in (st.mc as PackMapColc).si)
                si.remove(st)
            remove(st)
        }
        st.health

        holder.play.setOnClickListener {
            val intent = Intent(activity, BattlePrepare::class.java)
            intent.putExtra("Data", JsonEncoder.encode(st.id).toString())
            intent.putExtra("selection",0)
            activity.startActivity(intent)
        }

        return row
    }
    private fun getIcon(ene : Identifier<AbEnemy>) : Bitmap {
        if (ene.pack == Identifier.DEF) {
            return if (ene.id < (StaticStore.eicons?.size ?: 0)) StaticStore.eicons?.get(ene.id) ?: StaticStore.empty(context, 18f, 18f)
            else StaticStore.empty(context, 18f, 18f)
        }
        return (ene.get().preview?.img?.bimg() ?: StaticStore.empty(context, 18f, 18f)) as Bitmap
    }

    private fun getid(stage: SCDef): List<Identifier<AbEnemy>> {
        val result: MutableList<SCDef.Line?> = ArrayList()
        val data = reverse(stage.datas)
        for (datas in data) {
            if (result.isEmpty()) {
                result.add(datas)
                continue
            }
            val id = datas!!.enemy
            if (haveSame(id, result)) {
                result.add(datas)
            }
        }
        val ids: MutableList<Identifier<AbEnemy>> = ArrayList()
        for (datas in result) {
            datas ?: continue
            ids.add(datas.enemy)
        }
        return ids
    }

    private fun haveSame(id: Identifier<AbEnemy>, result: List<SCDef.Line?>): Boolean {
        if (id.pack == Identifier.DEF && (id.id == 19 || id.id == 20 || id.id == 21))
            return false

        for (data in result) {
            data ?: continue
            if (id.equals(data.enemy)) return false
        }
        return true
    }

    private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line?> {
        val result = arrayOfNulls<SCDef.Line>(data.size)
        for (i in data.indices) {
            result[i] = data[data.size - 1 - i]
        }
        return result
    }

    private fun getStageName(num: Int) : String {
        return "Stage"+number(num)
    }

    private fun number(num: Int): String {
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }
}