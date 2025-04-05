package com.yumetsuki.bcu.androidutil.stage.adapters

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import com.yumetsuki.bcu.BackgroundList
import com.yumetsuki.bcu.BattlePrepare
import com.yumetsuki.bcu.CastleList
import com.yumetsuki.bcu.ImageViewer
import com.yumetsuki.bcu.LimitEditor
import com.yumetsuki.bcu.MusicList
import com.yumetsuki.bcu.MusicPlayer
import com.yumetsuki.bcu.PackStageEnemyManager
import com.yumetsuki.bcu.PackStageManager
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.GetStrings
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.fakeandroid.CVGraphics
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import com.yumetsuki.bcu.androidutil.supports.WatcherEditText
import common.CommonStatic
import common.io.json.JsonEncoder
import common.pack.Identifier
import common.pack.UserProfile
import common.util.lang.MultiLangCont
import common.util.pack.Background
import common.util.stage.CastleImg
import common.util.stage.Music
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.stage.StageMap
import common.util.unit.AbEnemy
import kotlin.math.ceil
import kotlin.math.round

class CustomStageListAdapter(private val ctx: PackStageManager, private val map: StageMap) : RecyclerView.Adapter<CustomStageListAdapter.ViewHolder>() {

    class ViewHolder(private val ctx: PackStageManager, row: View) : RecyclerView.ViewHolder(row) {
        val name: WatcherEditText = row.findViewById(R.id.stagename)
        val icons: FlexboxLayout = row.findViewById(R.id.enemicon)
        val play: Button = row.findViewById(R.id.ch_stagePlay)
        val limit: Button = row.findViewById(R.id.ch_stageLimit)
        val enemies: Button = row.findViewById(R.id.ch_stageEnemy)

        val info: TableLayout = row.findViewById(R.id.cusstage_info)
        val expand: ImageButton = row.findViewById(R.id.cusstage_expand)

        val width: WatcherEditText = row.findViewById(R.id.ch_stwidth)
        val health: WatcherEditText = row.findViewById(R.id.ch_sthealth)
        val maxEne: WatcherEditText = row.findViewById(R.id.ch_stmaxEne)
        val dojo: Button = row.findViewById(R.id.ch_dojo)
        val bossguard: Button = row.findViewById(R.id.ch_bossguard)
        val nocon: Button = row.findViewById(R.id.ch_nocontinue)

        val mus: Button = row.findViewById(R.id.ch_mus)
        val mushp: WatcherEditText = row.findViewById(R.id.ch_mushp)
        val mush: Button = row.findViewById(R.id.ch_mush)

        val bg: Button = row.findViewById(R.id.ch_bg)
        val bghp: WatcherEditText = row.findViewById(R.id.ch_bghp)
        val bgh: Button = row.findViewById(R.id.ch_bgh)

        val ect: Button = row.findViewById(R.id.ch_ect)

        val uspawn: WatcherEditText = row.findViewById(R.id.ch_stuspwn)
        val espawn: WatcherEditText = row.findViewById(R.id.ch_stespwn)

        var st : Stage? = null
        var hit = false
        fun setStage(sta : Stage) {
            st = sta
        }

        @SuppressLint("SetTextI18n")
        fun addVal(thing : Any) {
            if (thing is Background) {
                if (hit) {
                    st!!.bg1 = thing.id
                    bgh.text = thing.toString()
                    bgh.setCompoundDrawablesWithIntrinsicBounds(getBGIcon(ctx, thing), null, null, null)
                } else {
                    st!!.bg = thing.id
                    bg.text = "${ctx.getString(R.string.stg_info_bg)}: $thing"
                    bg.setCompoundDrawablesWithIntrinsicBounds(getBGIcon(ctx, thing), null, null, null)
                }
            } else if (thing is Music) {
                if (hit) {
                    st!!.mus1 = thing.id
                    mush.text = thing.toString()
                } else {
                    st!!.mus0 = thing.id
                    mus.text = "${ctx.getString(R.string.stg_info_music)}: $thing"
                }
            } else if (thing is CastleImg) {
                st!!.castle = thing.id
                ect.text = "${ctx.getString(R.string.stg_info_ct)}: $thing"
                val w = if (thing.img.img.width >= thing.img.img.height) 40f else 40f * (thing.img.img.width.toFloat() / thing.img.img.height)
                val h = if (thing.img.img.height >= thing.img.img.width) 40f else 40f * (thing.img.img.height.toFloat() / thing.img.img.width)
                ect.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(ctx.resources, StaticStore.getResizeb(thing.img.img.bimg() as Bitmap, ctx, w, h)), null, null, null)
            }
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

    @SuppressLint("SetTextI18n")
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
        if(holder.name.text!!.toString().isBlank())
            holder.name.text = SpannableStringBuilder(getStageName(st.id()))
        holder.name.setWatcher {
            if (!holder.name.hasFocus() || st.names.toString() == holder.name.text!!.toString())
                return@setWatcher
            st.names.put(holder.name.text!!.toString())
        }
        holder.width.hint = "${ctx.getString(R.string.def_stg_length)}: ${st.len}"
        holder.width.setWatcher {
            val wid = CommonStatic.parseIntN(holder.width.text!!.toString())
            if (!holder.width.hasFocus() || wid == st.len)
                return@setWatcher
            val basepos = if (st.data.datas[st.data.datas.size - 1].castle_0 == 0)
                if (st.data.datas[st.data.datas.size - 1].boss >= 1)
                    ceil(Identifier.getOr(st.castle, CastleImg::class.java).boss_spawn).toInt()
                else 700
            else 800
            st.len = wid.coerceAtLeast(basepos + 800)
            for (line in st.data.datas) {
                line.doordis_0 = line.doordis_0.coerceAtMost(st.len - 500 - basepos)
                line.doordis_1 = line.doordis_1.coerceAtMost(st.len - 500 - basepos)
            }
        }
        holder.health.hint = if (st.trail)
            "${ctx.getString(R.string.def_base_health)}: ${st.timeLimit}"
        else
            "${ctx.getString(R.string.def_base_health)}: ${st.health}"
        holder.health.setWatcher {
            val wid = CommonStatic.parseIntN(holder.health.text!!.toString())
            if (!holder.health.hasFocus() || wid < 0)
                return@setWatcher
            if (st.trail)
                st.timeLimit = wid
            else
                st.health = wid
        }
        holder.maxEne.hint = "${ctx.getString(R.string.def_max_enemy)}: ${st.max}"
        holder.maxEne.setWatcher {
            val wid = CommonStatic.parseIntN(holder.maxEne.text!!.toString())
            if (!holder.maxEne.hasFocus() || wid <= 0 || wid == st.max)
                return@setWatcher
            st.max = wid
        }
        holder.espawn.hint = "${ctx.getString(R.string.min_respawn)}: ${st.minSpawn}f${if (st.minSpawn != st.maxSpawn) " ~ ${st.maxSpawn}f" else ""}"
        holder.espawn.setWatcher {
            val wid = CommonStatic.parseIntsN(holder.espawn.text!!.toString())
            if (!holder.maxEne.hasFocus() || wid.isEmpty())
                return@setWatcher
            val w2 = wid[if (wid.size >= 2) 1 else 0]
            if (wid[0] > 0 && w2 > 0)
                st.minSpawn = wid[0].coerceAtMost(w2)
            if (w2 > 0)
                st.maxSpawn = wid[0].coerceAtLeast(w2)
        }
        holder.uspawn.hint = "${ctx.getString(R.string.min_respawn)} (${ctx.getString(R.string.lineup_unit)}): ${st.minUSpawn}f${if (st.minUSpawn != st.maxUSpawn) " ~ ${st.maxUSpawn}f" else ""}"
        holder.uspawn.setWatcher {
            val wid = CommonStatic.parseIntsN(holder.uspawn.text!!.toString())
            if (!holder.maxEne.hasFocus() || wid.isEmpty())
                return@setWatcher
            val w2 = wid[if (wid.size >= 2) 1 else 0]
            if (wid[0] > 0 && w2 > 0)
                st.minUSpawn = wid[0].coerceAtMost(w2)
            if (w2 > 0)
                st.maxUSpawn = wid[0].coerceAtLeast(w2)
        }

        val s = GetStrings(ctx)

        holder.dojo.text = "${ctx.getString(R.string.stage_dojo)}: ${s.getBoolean(st.trail)}"
        holder.dojo.setOnClickListener {
            st.trail = !st.trail
            if (!st.trail)
                st.timeLimit = 0
            holder.health.text!!.clear()
            holder.health.hint = if (st.trail)
                "${ctx.getString(R.string.def_time_limit)}: ${st.timeLimit}"
            else
                "${ctx.getString(R.string.def_base_health)}: ${st.health}"
            holder.dojo.text = "${ctx.getString(R.string.stage_dojo)}: ${s.getBoolean(st.trail)}"
        }
        holder.bossguard.text = "${ctx.getString(R.string.boss_guard)}: ${s.getBoolean(st.bossGuard)}"
        holder.bossguard.setOnClickListener {
            st.bossGuard = !st.bossGuard
            holder.bossguard.text = "${ctx.getString(R.string.boss_guard)}: ${s.getBoolean(st.bossGuard)}"
        }
        holder.nocon.text = "${ctx.getString(R.string.stg_info_cont)}: ${s.getBoolean(!st.non_con)}"
        holder.nocon.setOnClickListener {
            st.non_con = !st.non_con
            holder.nocon.text = "${ctx.getString(R.string.stg_info_cont)}: ${s.getBoolean(!st.non_con)}"
        }
        holder.setStage(st)
        holder.resetIcons()

        holder.mus.text = "${ctx.getString(R.string.stg_info_music)}: ${st.mus0?.get()}"
        holder.mus.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                holder.hit = false
                ctx.notif = { holder.addVal(it) }

                val intent = Intent(ctx, MusicList::class.java)
                intent.putExtra("pack", map.cont.sid)
                ctx.resultLauncher.launch(intent)
            }
        })
        holder.mus.setOnLongClickListener {
            if (st.mus0?.get() == null)
                return@setOnLongClickListener false
            val intent = Intent(ctx, MusicPlayer::class.java)
            intent.putExtra("Data", JsonEncoder.encode(st.mus0).toString())
            ctx.startActivity(intent)
            false
        }

        holder.mushp.hint = "<${st.mush}%: "
        holder.mushp.setWatcher {
            val wid = CommonStatic.parseIntN(holder.mushp.text!!.toString())
            if (!holder.mushp.hasFocus() || wid < 0 || wid > 100)
                return@setWatcher
            if (wid == 0)
                st.mus1 = null
            st.mush = wid
        }

        holder.mush.text = "${st.mus1?.get()}"
        holder.mush.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if (st.mush == 0)
                    return
                holder.hit = true
                ctx.notif = { holder.addVal(it) }

                val intent = Intent(ctx, MusicList::class.java)
                intent.putExtra("pack", map.cont.sid)
                ctx.resultLauncher.launch(intent)
            }
        })
        holder.mush.setOnLongClickListener {
            if (st.mus1?.get() == null)
                return@setOnLongClickListener false
            val intent = Intent(ctx, MusicPlayer::class.java)
            intent.putExtra("Data", JsonEncoder.encode(st.mus1).toString())
            ctx.startActivity(intent)
            false
        }

        holder.bg.text = "${ctx.getString(R.string.stg_info_bg)}: ${st.bg?.get()}"
        holder.bg.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                holder.hit = false
                ctx.notif = { holder.addVal(it) }

                val intent = Intent(ctx, BackgroundList::class.java)
                intent.putExtra("pack", map.cont.sid)
                ctx.resultLauncher.launch(intent)
            }
        })
        holder.bg.setOnLongClickListener {
            if (st.bg?.get() == null)
                return@setOnLongClickListener false
            val intent = Intent(ctx, ImageViewer::class.java)
            if(st.bg.pack == Identifier.DEF)
                intent.putExtra("BGNum", UserProfile.getBCData().bgs.indexOf(st.bg.get()))

            intent.putExtra("Data", JsonEncoder.encode(st.bg).toString())
            intent.putExtra("Img", ImageViewer.ViewerType.BACKGROUND.name)

            ctx.startActivity(intent)
            false
        }
        if (st.bg?.get() != null)
            holder.bg.setCompoundDrawablesWithIntrinsicBounds(getBGIcon(ctx, st.bg.get()), null, null, null)

        holder.bghp.hint = "<${st.bgh}%: "
        holder.bghp.setWatcher {
            val wid = CommonStatic.parseIntN(holder.bghp.text!!.toString())
            if (!holder.bghp.hasFocus() || wid < 0 || wid > 100)
                return@setWatcher
            if (wid == 0)
                st.bg1 = null
            st.bgh = wid
        }

        holder.bgh.text = "${st.bg1?.get()}"
        holder.bgh.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if (st.bgh == 0)
                    return
                holder.hit = true
                ctx.notif = { holder.addVal(it) }

                val intent = Intent(ctx, BackgroundList::class.java)
                intent.putExtra("pack", map.cont.sid)
                ctx.resultLauncher.launch(intent)
            }
        })
        holder.bgh.setOnLongClickListener {
            if (st.bg1?.get() == null)
                return@setOnLongClickListener false
            val intent = Intent(ctx, ImageViewer::class.java)
            if(st.bg1.pack == Identifier.DEF)
                intent.putExtra("BGNum", UserProfile.getBCData().bgs.indexOf(st.bg1.get()))

            intent.putExtra("Data", JsonEncoder.encode(st.bg1).toString())
            intent.putExtra("Img", ImageViewer.ViewerType.BACKGROUND.name)

            ctx.startActivity(intent)
            false
        }
        if (st.bg1?.get() != null)
            holder.bgh.setCompoundDrawablesWithIntrinsicBounds(getBGIcon(ctx, st.bg1.get()), null, null, null)

        holder.ect.text = "${ctx.getString(R.string.stg_info_ct)}: ${st.castle?.get()}"
        holder.ect.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                ctx.notif = { holder.addVal(it) }

                val intent = Intent(ctx, CastleList::class.java)
                intent.putExtra("pack", map.cont.sid)
                ctx.resultLauncher.launch(intent)
            }
        })
        holder.ect.setOnLongClickListener {
            if (st.castle?.get() == null)
                return@setOnLongClickListener false
            val intent = Intent(ctx, ImageViewer::class.java)

            intent.putExtra("Data", JsonEncoder.encode(st.castle).toString())
            intent.putExtra("Img", ImageViewer.ViewerType.CASTLE.name)
            ctx.startActivity(intent)
            false
        }
        if (st.castle?.get() != null) {
            val img = st.castle.get().img.img
            val w = if (img.width >= img.height) 40f else 40f * (img.width.toFloat() / img.height)
            val h = if (img.height >= img.width) 40f else 40f * (img.height.toFloat() / img.width)
            holder.ect.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(ctx.resources, StaticStore.getResizeb(img.bimg() as Bitmap, ctx, w, h)), null, null, null)
        }

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

    companion object {
        private fun getBGIcon(ctx : PackStageManager, bg: Background): BitmapDrawable {
            val width = 40f
            val height = 40f

            val paint = Paint().apply {
                isFilterBitmap = true
                isAntiAlias = true
            }
            val b = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(b)

            bg.check()
            val cv = CVGraphics(canvas, Paint(), paint, false)

            if (bg.top) {
                val groundPart = bg.parts[Background.BG]
                val skyPart = bg.parts[Background.TOP]

                val totalHeight = groundPart.height + skyPart.height
                val ratio = height / totalHeight.toFloat()
                val imageWidth = round(groundPart.width * ratio)
                val groundHeight = round(groundPart.height * ratio)
                val skyHeight = round(skyPart.height * ratio)

                var groundGradient = round(height * 0.1f)
                val skyGradient = round(height * 0.1f)
                if (groundGradient + groundHeight + skyHeight + skyGradient != height)
                    groundGradient += height - (groundGradient + groundHeight + skyHeight + skyGradient)

                var currentX = 0f
                while (currentX < width) {
                    cv.drawImage(skyPart, currentX, skyGradient, imageWidth, skyHeight)
                    cv.drawImage(groundPart, currentX, skyGradient + skyHeight, imageWidth, groundHeight)
                    currentX += imageWidth
                }

                cv.gradRect(0f, skyGradient + skyHeight + groundHeight, width, groundGradient, 0f,
                    skyGradient + skyHeight + groundHeight, getColorData(bg, 2), 0f, height, getColorData(bg, 3))
                cv.gradRect(0f, 0f, width, skyGradient, 0f, 0f, getColorData(bg, 0), 0f,
                    skyGradient, getColorData(bg, 1))
            } else {
                val groundPart = bg.parts[Background.BG]
                val ratio = height / (groundPart.height * 2f)

                val imageWidth = round(groundPart.width * ratio)
                val groundHeight = round(groundPart.height * ratio)
                var groundGradient = round(height * 0.1f)
                val skyGradient = round(height * 0.1f + groundHeight)
                if (groundGradient + groundHeight + skyGradient != height)
                    groundGradient += height - (groundGradient + groundHeight + skyGradient)

                var currentX = 0f

                while (currentX < width) {
                    cv.drawImage(groundPart, currentX, skyGradient, imageWidth, groundHeight)
                    currentX += imageWidth
                }
                cv.gradRect(0f, skyGradient + groundHeight, width, groundGradient, 0f, skyGradient + groundHeight,
                    getColorData(bg, 2), 0f, height, getColorData(bg, 3))
                cv.gradRect(0f, 0f, width, skyGradient, 0f, 0f, getColorData(bg, 0), 0f, skyGradient,
                    getColorData(bg, 1))
            }
            bg.unload()
            return BitmapDrawable(ctx.resources, b)
        }

        private fun getColorData(bg: Background, mode: Int) : IntArray {
            bg.cs ?: return intArrayOf(0, 0, 0)

            if(bg.cs.isEmpty())
                return intArrayOf(0, 0, 0)

            return when(mode) {
                0 -> bg.cs[0] ?: return intArrayOf(0, 0, 0)
                1 -> bg.cs[1] ?: return intArrayOf(0, 0, 0)
                2 -> bg.cs[2] ?: return intArrayOf(0, 0, 0)
                3 -> bg.cs[3] ?: return intArrayOf(0, 0, 0)
                else -> intArrayOf(0, 0, 0)
            }
        }
    }
}