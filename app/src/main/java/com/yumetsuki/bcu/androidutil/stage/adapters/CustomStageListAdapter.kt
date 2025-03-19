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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.ToggleButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.yumetsuki.bcu.BattlePrepare
import com.yumetsuki.bcu.LimitEditor
import com.yumetsuki.bcu.PackStageEnemyManager
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.util.lang.MultiLangCont
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.stage.StageMap
import common.util.unit.AbEnemy

class CustomStageListAdapter(private val ctx: Activity, private val map: StageMap) : RecyclerView.Adapter<CustomStageListAdapter.ViewHolder>() {

    class ViewHolder(private val ctx: Activity, row: View) : RecyclerView.ViewHolder(row) {
        val name: EditText = row.findViewById(R.id.stagename)
        val icons: FlexboxLayout = row.findViewById(R.id.enemicon)
        val play: Button = row.findViewById(R.id.ch_stagePlay)
        val limit: Button = row.findViewById(R.id.ch_stageLimit)
        val enemies: Button = row.findViewById(R.id.ch_stageEnemy)

        val info: TableLayout = row.findViewById(R.id.cusstage_info)
        val expand: ImageButton = row.findViewById(R.id.cusstage_expand)

        val width: EditText = row.findViewById(R.id.ch_stwidth)
        val health: EditText = row.findViewById(R.id.ch_sthealth)
        val maxEne: EditText = row.findViewById(R.id.ch_stmaxEne)
        val dojo: ToggleButton = row.findViewById(R.id.ch_dojo)
        val bossguard: ToggleButton = row.findViewById(R.id.ch_bossguard)

        var st : Stage? = null
        fun setStage(sta : Stage) {
            st = sta
        }

        fun resetIcons() {
            icons.removeAllViews()
            val ids = getid(st?.data ?: return)
            if (ids.isNotEmpty())
                for (i in ids.indices) {
                    val icn = getIcon(ids[i])
                    val icon = ImageView(ctx)
                    icon.layoutParams = FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    icon.setImageBitmap(icn)
                    icon.setPadding(StaticStore.dptopx(12f, ctx), StaticStore.dptopx(4f, ctx), 0, StaticStore.dptopx(4f, ctx))
                    icons.addView(icon)
                }
        }

        private fun getIcon(ene : Identifier<AbEnemy>) : Bitmap {
            if (ene.pack == Identifier.DEF) {
                return if (ene.id < (StaticStore.eicons?.size ?: 0)) StaticStore.eicons?.get(ene.id) ?: StaticStore.empty(ctx, 18f, 18f)
                else StaticStore.empty(ctx, 18f, 18f)
            }
            return (ene.get().preview?.img?.bimg() ?: StaticStore.empty(ctx, 18f, 18f)) as Bitmap
        }

        private fun getid(stage: SCDef): List<Identifier<AbEnemy>> {
            val result: MutableList<SCDef.Line?> = ArrayList()
            val data = reverse(stage.datas)
            for (datas in data) {
                if (result.isEmpty()) {
                    result.add(datas)
                    continue
                }
                val id = datas.enemy
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

        private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line> {
            return Array(data.size) { data[data.size - 1 - it] }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.cus_stage_info_layout, viewGroup, false)
        return ViewHolder(ctx, row)
    }

    override fun getItemCount(): Int {
        return map.list.size()
    }

    override fun onBindViewHolder(holder: ViewHolder, indx: Int) {
        val pos = holder.bindingAdapterPosition
        val st = map.list[pos] ?: return

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
                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_more_black_24dp))
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
                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_less_black_24dp))
            }
        })

        holder.name.text = SpannableStringBuilder(MultiLangCont.get(st) ?: st.names.toString())
        if(holder.name.text.isBlank())
            holder.name.text = SpannableStringBuilder(getStageName(st.id()))
        holder.name.setOnEditorActionListener { _, _, _ ->
            if (st.names.toString() == holder.name.text.toString())
                return@setOnEditorActionListener false
            st.names.put(holder.name.text.toString())
            false
        }
        holder.width.hint = "${ctx.getString(R.string.def_stg_length)}: ${st.len}"
        holder.width.setOnEditorActionListener { _, _, _ ->
            val wid = CommonStatic.parseIntN(holder.width.text.toString())
            if (wid < 2000 || wid == st.len)
                return@setOnEditorActionListener false
            st.len = wid
            false
        }
        holder.health.hint = if (st.trail)
            "${ctx.getString(R.string.def_base_health)}: ${st.timeLimit}"
        else
            "${ctx.getString(R.string.def_base_health)}: ${st.health}"
        holder.health.setOnEditorActionListener { _, _, _ ->
            val wid = CommonStatic.parseIntN(holder.health.text.toString())
            if (wid < 0)
                return@setOnEditorActionListener false
            if (st.trail)
                st.timeLimit = wid
            else
                st.health = wid
            false
        }
        holder.maxEne.hint = "${ctx.getString(R.string.def_max_enemy)}: ${st.max}"
        holder.maxEne.setOnEditorActionListener { _, _, _ ->
            val wid = CommonStatic.parseIntN(holder.maxEne.text.toString())
            if (wid <= 0 || wid == st.max)
                return@setOnEditorActionListener false
            st.max = wid
            false
        }
        holder.dojo.isChecked = st.trail
        holder.dojo.setOnClickListener {
            st.trail = holder.dojo.isChecked
            if (!st.trail)
                st.timeLimit = 0
            holder.health.text.clear()
            holder.health.hint = if (st.trail)
                "${ctx.getString(R.string.def_time_limit)}: ${st.timeLimit}"
            else
                "${ctx.getString(R.string.def_base_health)}: ${st.health}"
        }
        holder.bossguard.isChecked = st.bossGuard
        holder.bossguard.setOnClickListener {
            st.bossGuard = holder.bossguard.isChecked
        }
        holder.setStage(st)
        holder.resetIcons()

        holder.play.setOnClickListener {
            val intent = Intent(ctx, BattlePrepare::class.java)
            intent.putExtra("Data", JsonEncoder.encode(st.id).toString())
            intent.putExtra("selection",0)
            ctx.startActivity(intent)
        }
        holder.limit.setOnClickListener {
            LimitEditor.lim = st.lim
            val intent = Intent(ctx, LimitEditor::class.java)
            intent.putExtra("name", "$st ${ctx.getString(R.string.stg_info_limit)}: ${st.lim}")

            ctx.startActivity(intent)
        }
        holder.enemies.setOnClickListener {
            val intent = Intent(ctx, PackStageEnemyManager::class.java)
            intent.putExtra("stage", JsonEncoder.encode(st.id).toString())
            ctx.startActivity(intent)
        }
    }

    private fun getStageName(num: Int) : String {
        return "Stage"+number(num)
    }

    private fun number(num: Int): String {
        return if (num in 0..9) "00$num" else if (num in 10..99) "0$num" else "" + num
    }
}