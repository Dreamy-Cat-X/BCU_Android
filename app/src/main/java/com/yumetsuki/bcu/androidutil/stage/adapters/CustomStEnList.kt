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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.yumetsuki.bcu.EnemyInfo
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.GetStrings
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.unit.Enemy
import org.jcodec.common.tools.MathUtil

class CustomStEnList(private val ctx: Activity, private val st: Stage) : RecyclerView.Adapter<CustomStEnList.ViewHolder>() {

    override fun onCreateViewHolder(group: ViewGroup, i: Int): ViewHolder {
        val row = LayoutInflater.from(ctx).inflate(R.layout.cus_stage_enemy_list_layout, group, false)
        return ViewHolder(row)
    }

    override fun getItemCount(): Int {
        return st.data.datas.size
    }

    override fun onBindViewHolder(holder: ViewHolder, i: Int) {
        val s = GetStrings(ctx)
        val data = reverse(st.data.datas)

        val pos = holder.bindingAdapterPosition

        holder.expand.setOnClickListener(View.OnClickListener {
            if (SystemClock.elapsedRealtime() - StaticStore.infoClick < StaticStore.INFO_INTERVAL)
                return@OnClickListener

            StaticStore.infoClick = SystemClock.elapsedRealtime()

            if (holder.moreinfo.height == 0) {
                holder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

                val height = holder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(0, height)

                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.moreinfo.layoutParams
                    layout.height = `val`
                    holder.moreinfo.layoutParams = layout
                }

                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()

                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_more_black_24dp))
            } else {
                holder.moreinfo.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                val height = holder.moreinfo.measuredHeight
                val anim = ValueAnimator.ofInt(height, 0)
                anim.addUpdateListener { animation ->
                    val `val` = animation.animatedValue as Int
                    val layout = holder.moreinfo.layoutParams
                    layout.height = `val`
                    holder.moreinfo.layoutParams = layout
                }
                anim.duration = 300
                anim.interpolator = DecelerateInterpolator()
                anim.start()
                holder.expand.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.ic_expand_less_black_24dp))
            }
        })

        val id = data[pos].enemy ?: UserProfile.getBCData().enemies[0].id
        val em = Identifier.get(id) ?: return

        val icon = em.icon?.img?.bimg()
        if(icon == null) {
            holder.icon.setImageBitmap(StaticStore.empty(ctx, 85f, 32f))
        } else
            holder.icon.setImageBitmap(StaticStore.getResizeb(icon as Bitmap, ctx, 85f, 32f))

        holder.number.hint = s.getNumber(data[pos])
        holder.number.setOnEditorActionListener {_, _, _ ->
            val num = CommonStatic.parseIntN(holder.number.text.ifBlank { holder.number.hint }.toString())
            if (num >= 0 && num != data[pos].number)
                data[pos].number = num
            false
        }
        holder.multiply.text = SpannableStringBuilder(s.getMultiply(data[pos], 100))
        holder.multiply.setOnEditorActionListener {_, _, _ ->
            val nums = CommonStatic.parseIntsN(holder.multiply.text.toString())
            if (nums.isEmpty()) return@setOnEditorActionListener false
            val atk = if (nums.size >= 2) nums[1] else nums[0]

            if (nums[0] >= 0 && nums[0] != data[pos].multiple)
                data[pos].multiple = nums[0]
            if (atk >= 0 && atk != data[pos].mult_atk)
                data[pos].mult_atk = atk
            false
        }
        holder.bh.hint = s.getBaseHealth(data[pos])
        holder.bh.setOnEditorActionListener {_, _, _ ->
            val num = CommonStatic.parseIntN(holder.bh.text.ifBlank { holder.bh.hint }.toString())
            if (num >= 0 && num != data[pos].castle_0)
                data[pos].castle_0 = num
            false
        }
        holder.isboss.text = SpannableStringBuilder(data[pos].boss.toString())
        holder.isboss.setOnEditorActionListener {_, _, _ ->
            val num = MathUtil.clip(CommonStatic.parseIntN(holder.isboss.text.ifBlank { holder.isboss.hint }.toString()), 0, 2)
            if (num != data[pos].boss)
                data[pos].boss = num
            false
        }
        holder.layer.text = SpannableStringBuilder(s.getLayer(data[pos]))
        holder.layer.setOnEditorActionListener {_, _, _ ->
            val nums = CommonStatic.parseIntsN(holder.layer.text.toString())
            if (nums.isEmpty()) return@setOnEditorActionListener false
            val lay1 = if (nums.size >= 2) nums[1] else nums[0]

            if (nums[0] != data[pos].layer_0)
                data[pos].layer_0 = nums[0]
            if (lay1 != data[pos].layer_1)
                data[pos].layer_1 = lay1
            false
        }
        holder.start.text = SpannableStringBuilder(s.getStart(data[pos], true))
        holder.start.setOnEditorActionListener {_, _, _ ->
            val nums = CommonStatic.parseIntsN(holder.start.text.toString())
            if (nums.isEmpty()) return@setOnEditorActionListener false
            val spa1 = if (nums.size >= 2) nums[1] else nums[0]

            if (nums[0] >= 0 && nums[0] != data[pos].spawn_0)
                data[pos].spawn_0 = nums[0]
            if (spa1 >= 0 && spa1 != data[pos].spawn_1)
                data[pos].spawn_1 = spa1
            false
        }
        holder.respawn.text = SpannableStringBuilder(s.getRespawn(data[pos], true))
        holder.respawn.setOnEditorActionListener {_, _, _ ->
            val nums = CommonStatic.parseIntsN(holder.respawn.text.toString())
            if (nums.isEmpty()) return@setOnEditorActionListener false
            val spa1 = if (nums.size >= 2) nums[1] else nums[0]

            if (nums[0] >= 0 && nums[0] != data[pos].respawn_0)
                data[pos].respawn_0 = nums[0]
            if (spa1 >= 0 && spa1 != data[pos].respawn_1)
                data[pos].respawn_1 = spa1
            false
        }
        holder.killcount.hint = data[pos].kill_count.toString()
        holder.killcount.setOnEditorActionListener {_, _, _ ->
            val num = CommonStatic.parseIntN(holder.killcount.text.ifBlank { holder.killcount.hint }.toString())
            if (num >= 0 && num != data[pos].kill_count)
                data[pos].kill_count = num
            false
        }
        val build = StringBuilder(data[pos].doorchance.toString()).append("%")
        if (data[pos].doorchance > 0) {
            build.append(": ").append(data[pos].doordis_0).append("%")
            if (data[pos].doordis_0 != data[pos].doordis_1)
                build.append(" ~ ").append(data[pos].doordis_1).append("%")
        }
        holder.edoor.text = SpannableStringBuilder(build.toString())
        holder.edoor.setOnEditorActionListener {_, _, _ ->
            val nums = CommonStatic.parseIntsN(holder.edoor.text.toString())
            if (nums.isEmpty()) return@setOnEditorActionListener false
            if (nums[0] >= 0 && nums[0] != data[pos].respawn_0)
                data[pos].doorchance = nums[0].toByte()
            if (nums.size == 1) {
                return@setOnEditorActionListener false
            }
            val spa1 = if (nums.size >= 3) nums[2] else nums[1]

            if (nums[1] >= 0 && nums[1].toByte() != data[pos].doordis_0)
                data[pos].doordis_0 = nums[1].toByte()
            if (spa1 >= 0 && spa1.toByte() != data[pos].doordis_1)
                data[pos].doordis_1 = spa1.toByte()
            false
        }

        if (em is Enemy) {
            holder.info.setOnClickListener(object : SingleClick() {
                override fun onSingleClick(v: View?) {
                    val intent = Intent(ctx, EnemyInfo::class.java)
                    intent.putExtra("Data", JsonEncoder.encode(em.id).toString())
                    intent.putExtra("Multiply", (data[pos].multiple.toFloat()).toInt())
                    intent.putExtra("AMultiply", (data[pos].mult_atk.toFloat()).toInt())
                    ctx.startActivity(intent)
                }
            })
        }
    }

    class ViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val expand = row.findViewById<ImageButton>(R.id.stgenlistexp)!!
        val icon = row.findViewById<ImageView>(R.id.stgenlisticon)!!
        val multiply = row.findViewById<EditText>(R.id.stgenlistmultir)!!
        val number = row.findViewById<EditText>(R.id.stgenlistnumr)!!
        val info = row.findViewById<ImageButton>(R.id.stgenlistinfo)!!
        val bh = row.findViewById<EditText>(R.id.enemlistbhr)!!
        val isboss = row.findViewById<EditText>(R.id.enemlistibr)!!
        val layer = row.findViewById<EditText>(R.id.enemlistlayr)!!
        val start = row.findViewById<EditText>(R.id.enemliststr)!!
        val respawn = row.findViewById<EditText>(R.id.enemlistresr)!!
        val moreinfo = row.findViewById<TableLayout>(R.id.stgenlistmi)!!
        val killcount = row.findViewById<EditText>(R.id.enemlistkilcr)!!
        val edoor = row.findViewById<EditText>(R.id.enemlistevrdr)!!
    }

    private fun reverse(data: Array<SCDef.Line>): Array<SCDef.Line> {
        return Array(data.size) { data[data.size - 1 - it] }
    }
}