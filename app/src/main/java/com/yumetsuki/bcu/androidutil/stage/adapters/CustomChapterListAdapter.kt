package com.yumetsuki.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
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
import android.widget.TableLayout
import android.widget.TableRow
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.yumetsuki.bcu.PackStageManager
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.supports.DynamicListView.StableArrayAdapter
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.PackData.UserPack
import common.util.stage.StageMap
import common.util.stage.info.CustomStageInfo


class CustomChapterListAdapter(private val pack : UserPack, private val ctx: Activity) : StableArrayAdapter<StageMap>(ctx, R.layout.cus_chapter_info_layout, pack.mc.maps.list) {

    internal class ViewHolder(row: View) {
        var chName: TextInputEditText = row.findViewById(R.id.cuschapter_name)
        var starRow: TableRow = row.findViewById(R.id.pk_chapterStarRow)
        var info: TableLayout = row.findViewById(R.id.cuschapter_info)
        var expand: ImageButton = row.findViewById(R.id.cuschapter_expand)

        var cost: EditText = row.findViewById(R.id.pk_chapterCost)
        var stageEdit: Button = row.findViewById(R.id.pk_stageEdit)
        var limitEdit: Button = row.findViewById(R.id.pk_limitEdit)
        var delete: FloatingActionButton = row.findViewById(R.id.pk_deleteChapter)
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

        holder.chName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_DONE || subchapter.names.toString() == holder.chName.text.toString())
                return@setOnEditorActionListener false
            subchapter.names.put(holder.chName.text.toString())
            false
        }
        holder.chName.text = SpannableStringBuilder(subchapter.names.toString())

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

        for (s in 0..3) {
            val stDif = holder.starRow.getChildAt(s) as EditText
            stDif.visibility = if (s > subchapter.stars.size)
                View.GONE else View.VISIBLE

            stDif.hint = "$s★: ${if (s < subchapter.stars.size) subchapter.stars[s] else 0}%"
            stDif.setOnEditorActionListener { _, actionId, _ ->
                val star = CommonStatic.parseIntN(stDif.text.toString().ifBlank { stDif.hint.toString() })
                if (star < 0 || actionId != EditorInfo.IME_ACTION_DONE)
                    return@setOnEditorActionListener false

                if (s == subchapter.stars.size-1 && star == 0) {
                    subchapter.stars = subchapter.stars.copyOf(subchapter.stars.size - 1)
                    if (s < 3)
                        holder.starRow.getChildAt(s+1).visibility = View.GONE
                } else if (star > 0) {
                    if (s == subchapter.stars.size) {
                        subchapter.stars = subchapter.stars.copyOf(subchapter.stars.size + 1)
                        if (s < 3)
                            holder.starRow.getChildAt(s+1).visibility = View.VISIBLE
                    }
                    subchapter.stars[s] = star
                }
                false
            }
        }
        holder.cost.hint = "${ctx.getString(R.string.cuschapter_cost)}: ${subchapter.price}"
        holder.chName.setOnEditorActionListener { _, actionId, _ ->
            val cost = CommonStatic.parseIntN(holder.chName.text.toString().ifBlank { holder.cost.hint.toString() })
            if (cost < 0 || actionId != EditorInfo.IME_ACTION_DONE)
                return@setOnEditorActionListener false
            subchapter.price = cost
            false
        }
        holder.delete.setOnClickListener {
            pack.mc.maps.remove(subchapter)
            remove(subchapter)
            for (s in subchapter.list) {
                if (s.info != null)
                    (s.info as CustomStageInfo).destroy(false)
                for (si in pack.mc.si)
                    si.remove(s)
            }
            notifyDataSetChanged()
        }

        holder.stageEdit.setOnClickListener {
            val intent = Intent(context, PackStageManager::class.java)
            intent.putExtra("map", JsonEncoder.encode(subchapter).toString())
            ctx.startActivity(intent)
        }
        //holder.limitEdit.setOnClickListener { //TODO: Limit List instead
        //val intent = Intent(context, LimitEditor::class.java)
        //intent.putExtra("map", JsonEncoder.encode(subchapter).toString())
        //ac.startActivity(intent)
        //}

        return row
    }
}