package com.yumetsuki.bcu.androidutil

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.yumetsuki.bcu.R
import common.battle.BasisSet
import common.battle.Treasure
import common.battle.data.CustomEntity
import common.battle.data.DataEnemy
import common.battle.data.DataUnit
import common.battle.data.MaskEntity
import common.battle.data.MaskUnit
import common.pack.Identifier
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.stage.Limit
import common.util.stage.SCDef
import common.util.stage.Stage
import common.util.stage.info.DefStageInfo
import common.util.unit.Enemy
import common.util.unit.Form
import common.util.unit.Level
import java.text.DecimalFormat
import kotlin.math.roundToInt

class GetStrings(private val c: Context) {
    companion object {
        private val talData = intArrayOf(
            -1, //0: ??
            R.string.sch_abi_we, //1: Weaken
            R.string.sch_abi_fr, //2: Freeze
            R.string.sch_abi_sl, //3: Slow
            R.string.sch_abi_ao, //4: Attacks Only
            R.string.sch_abi_st, //5: Strong
            R.string.sch_abi_re, //6: Resistant
            R.string.sch_abi_md, //7: Massive Damage
            R.string.sch_abi_kb, //8: Knockback
            R.string.sch_abi_wa, //9: Warp
            R.string.sch_abi_str, //10: Strengthen
            R.string.sch_abi_su, //11: Survive
            R.string.sch_abi_bd, //12: Base Destroyer
            R.string.sch_abi_cr, //13: Critical
            R.string.sch_abi_zk, //14: Zombie Killer
            R.string.sch_abi_bb, //15: Barrier Breaker
            R.string.sch_abi_em, //16: Extra Money
            R.string.sch_abi_wv, //17: Wave
            R.string.talen_we, //18: Res. Weaken
            R.string.talen_fr, //19: Res. Freeze
            R.string.talen_sl, //20: Res. Slow
            R.string.talen_kb, //21: Res. Knockback
            R.string.talen_wv, //22: Res. Wave
            R.string.sch_abi_ws, //23: Wave Shield
            R.string.talen_warp, //24: Res. Warp
            R.string.unit_info_cost, //25: Cost
            R.string.unit_info_cd, //26: Cooldown
            R.string.unit_info_spd, //27: Speed
            R.string.hb, //28: ??
            R.string.sch_abi_ic, //29: Imu. Curse
            R.string.talen_cu, //30: Res. Curse
            R.string.unit_info_atk, //31: Attack Damage
            R.string.unit_info_hp, //32: HP
            R.string.sch_red, //33: Red Trait
            R.string.sch_fl, //34: Float Trait
            R.string.sch_bla, //35: Black Trait
            R.string.sch_me, //36: Metal Trait
            R.string.sch_an, //37: Angel Trait
            R.string.sch_al, //38: Alien Trait
            R.string.sch_zo, //39: Zombie Trait
            R.string.sch_re, //40: Relic Trait
            R.string.sch_wh, //41: White Trait
            -1, //42: ??
            -1, //43: ??
            R.string.sch_abi_iw, //44: Imu. Weaken
            R.string.sch_abi_if, //45: Imu. Freeze
            R.string.sch_abi_is, //46: Imu. Slow
            R.string.sch_abi_ik, //47: Imu. Knockback
            R.string.sch_abi_iwv, //48: Imu. Wave
            R.string.sch_abi_iwa, //49: Imu. Warp
            R.string.sch_abi_sb, //50: Savage Blow
            R.string.sch_abi_iv, //51: Invincibility
            R.string.talen_poi, //52: Res. Poison
            R.string.abi_ipoi, //53: Imu. Poison
            R.string.talen_sur, //54: Res. Surge
            R.string.sch_abi_imsu, //55: Imu. Surge
            R.string.sch_abi_surge, //56: Surge Attack,
            R.string.sch_de, //57: Aku
            R.string.sch_abi_shb, //58: Shield breaker
            R.string.sch_abi_ck, //59: Corpse killer
            R.string.sch_abi_cu, //60: Curse
            R.string.unit_info_tba, //61: TBA
            R.string.sch_abi_mw, //62: Mini Wave
            R.string.sch_abi_bk, //63: Colossus Slayer
            R.string.sch_abi_bh, //64: Behemoth Hunter
            R.string.sch_abi_ms, //65: Mini Surge
            R.string.sch_abi_sh //66: Super Sage Slayer
        )
        private lateinit var talTool: Array<String>
        private val mapcolcid = arrayOf("N", "S", "C", "CH", "E", "T", "V", "R", "M", "A", "B", "RA", "H", "CA", "Q")
        val mapcodes = listOf("000000", "000001", "000002", "000003", "000004", "000006", "000007", "000011", "000012", "000013", "000014", "000024", "000025", "000027", "000031")
        private val allColor: String
        private val allTrait: String

        init {
            val ac = StringBuilder()
            val at = StringBuilder()

            for (i in Interpret.traitMask.indices) {
                if(Interpret.traitMask[i] == Data.TRAIT_EVA || Interpret.traitMask[i] == Data.TRAIT_WITCH)
                    continue

                if (Interpret.traitMask[i] != Data.TRAIT_WHITE)
                    ac.append(Interpret.TRAIT[i]).append(", ")

                at.append(Interpret.TRAIT[i]).append(", ")
            }


            allColor = ac.toString()
            allTrait = at.toString()
        }
    }

    init {
        talTool = Array(talData.size) { i ->
            if(talData[i] == -1)
                return@Array "Invalid"

            c.getString(talData[i])
        }
    }

    fun getTitle(f: Form?): String {
        if (f == null)
            return ""

        val result = StringBuilder()

        val name = MultiLangCont.get(f) ?: f.names.toString()

        val rarity: String = when (f.unit.rarity) {
            0 -> c.getString(R.string.sch_rare_ba)
            1 -> c.getString(R.string.sch_rare_ex)
            2 -> c.getString(R.string.sch_rare_ra)
            3 -> c.getString(R.string.sch_rare_sr)
            4 -> c.getString(R.string.sch_rare_ur)
            5 -> c.getString(R.string.sch_rare_lr)
            else -> "Unknown"
        }
        return if (name == "") rarity else result.append(rarity).append(" - ").append(name).toString()
    }

    fun getAtkTime(f: Form?, talent: Boolean, frse: Int, lvs: Level): String {
        if (f == null)
            return ""

        val du = if(f.du.pCoin != null && talent)
            f.du.pCoin.improve(lvs.talents)
        else
            f.du

        return if (frse == 0)
            du.getItv(0).toString() + "f"
        else
            DecimalFormat("#.##").format(du.getItv(0).toDouble() / 30) + "s"
    }

    fun getAtkTime(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.getItv(0).toString() + "f" else DecimalFormat("#.##").format(em.de.getItv(0).toDouble() / 30) + "s"
    }

    fun getAbilT(f: Form?): String {
        if (f == null) return ""
        val atkdat = f.du.getAtks(0)
        val result = StringBuilder()
        for (i in atkdat.indices) {
            if (i != atkdat.size - 1) {
                if (atkdat[i].canProc()) result.append(c.getString(R.string.unit_info_true)).append(" / ") else result.append(c.getString(R.string.unit_info_false)).append(" / ")
            } else {
                if (atkdat[i].canProc()) result.append(c.getString(R.string.unit_info_true)) else result.append(c.getString(R.string.unit_info_false))
            }
        }
        return result.toString()
    }

    fun getAbilT(em: Enemy?): String {
        if (em == null) return ""
        val atks = em.de.getAtks(0)
        val result = StringBuilder()
        for (i in atks.indices) {
            if (i < atks.size - 1) {
                if (atks[i].canProc()) result.append(c.getString(R.string.unit_info_true)).append(" / ") else result.append(c.getString(R.string.unit_info_false)).append(" / ")
            } else {
                if (atks[i].canProc()) result.append(c.getString(R.string.unit_info_true)) else result.append(c.getString(R.string.unit_info_false))
            }
        }
        return result.toString()
    }

    fun getPost(f: Form?, frse: Int): String {
        if (f == null) return ""
        return if (frse == 0) f.du.getPost(false, 0).toString() + "f" else DecimalFormat("#.##").format(f.du.getPost(false, 0).toDouble() / 30) + "s"
    }

    fun getPost(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.getPost(false, 0).toString() + "f" else DecimalFormat("#.##").format(em.de.getPost(false, 0).toDouble() / 30) + "s"
    }

    fun getTBA(f: Form?, talent: Boolean, frse: Int, lvs: Level): String {
        if (f == null)
            return ""

        val du = if(f.du.pCoin != null && talent)
            f.du.pCoin.improve(lvs.talents)
        else
            f.du

        return if (frse == 0)
            du.tba.toString() + "f"
        else
            DecimalFormat("#.##").format(du.tba.toDouble() / 30) + "s"
    }

    fun getTBA(em: Enemy?, frse: Int): String {
        if (em == null) return ""
        return if (frse == 0) em.de.tba.toString() + "f" else DecimalFormat("#.##").format(em.de.tba.toDouble() / 30) + "s"
    }

    fun getPre(f: Form?, frse: Int): String {
        if (f == null)
            return ""

        val atkdat = f.du.getAtks(0)
        return if (frse == 0) {
            if (atkdat.size > 1) {
                val result = StringBuilder()

                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1)
                        result.append(atkdat[i].pre).append("f / ")
                    else
                        result.append(atkdat[i].pre).append("f")
                }
                result.toString()
            } else atkdat[0].pre.toString() + "f"
        } else {
            if (atkdat.size > 1) {
                val result = StringBuilder()

                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1)
                        result.append(DecimalFormat("#.##").format(atkdat[i].pre.toDouble() / 30)).append("s / ")
                    else
                        result.append(DecimalFormat("#.##").format(atkdat[i].pre.toDouble() / 30)).append("s")
                }

                result.toString()

            } else
                DecimalFormat("#.##").format(atkdat[0].pre.toDouble() / 30) + "s"
        }
    }

    fun getPre(em: Enemy?, frse: Int): String {
        if (em == null)
            return ""

        val atkdat = em.de.getAtks(0)

        return if (frse == 0) {
            if (atkdat.size > 1) {
                val result = StringBuilder()

                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1)
                        result.append(atkdat[i].pre).append("f / ")
                    else
                        result.append(atkdat[i].pre).append("f")
                }
                result.toString()
            } else
                atkdat[0].pre.toString() + "f"
        } else {
            if (atkdat.size > 1) {
                val result = StringBuilder()

                for (i in atkdat.indices) {
                    if (i != atkdat.size - 1)
                        result.append(DecimalFormat("#.##").format(atkdat[i].pre.toDouble() / 30)).append("s / ")
                    else
                        result.append(DecimalFormat("#.##").format(atkdat[i].pre.toDouble() / 30)).append("s")
                }

                result.toString()

            } else
                DecimalFormat("#.##").format(atkdat[0].pre.toDouble() / 30) + "s"
        }
    }

    fun getPackName(id: Identifier<*>, isRaw: Boolean) : String {
        return if(isRaw) {
            id.pack
        } else {
            if(id.pack == Identifier.DEF) {
                c.getString(R.string.pack_default)
            } else {
                StaticStore.getPackName(id.pack)
            }
        }
    }

    fun getPackName(pack: String, isRaw: Boolean) : String {
        return if(isRaw) {
            pack
        } else {
            if(pack == Identifier.DEF || mapcodes.contains(pack)) {
                c.getString(R.string.pack_default)
            } else {
                StaticStore.getPackName(pack)
            }
        }
    }

    fun getID(viewHolder: RecyclerView.ViewHolder?, id: String): String {
        return if (viewHolder == null)
            ""
        else
            id + "-" + viewHolder.bindingAdapterPosition
    }

    fun getID(form: Int, id: String): String {
        return "$id-$form"
    }

    fun getRange(f: Form?): String {
        if (f == null)
            return ""

        val tb = f.du.range

        if(!f.du.isLD && !f.du.isOmni)
            return tb.toString()

        if(f.unit.id.pack != Identifier.DEF) {
            val du = f.du as CustomEntity

            val ma = if(du.getAtkCount(0) == 1) {
                du.getAtkModel(0, 0)
            } else if(allRangeSame(du)) {
                du.getAtkModel(0, 0)
            } else {
                du.repAtk
            }

            val lds = ma.shortPoint

            val ldr = ma.longPoint - ma.shortPoint

            val start = lds.coerceAtMost(lds + ldr)
            val end = lds.coerceAtLeast(lds + ldr)

            return "$tb | $start ~ $end"
        } else {
            val du = f.du as DataUnit

            if(du.getAtkCount(0) == 0 || allRangeSame(du)) {
                val ma = du.getAtkModel(0, 0)

                val lds = ma.shortPoint

                val ldr = ma.longPoint - ma.shortPoint

                val start = lds.coerceAtMost(lds + ldr)
                val end = lds.coerceAtLeast(lds + ldr)

                return "$tb | $start ~ $end"
            } else {
                val builder = StringBuilder("$tb | ")

                for(i in 0 until du.getAtkCount(0)) {
                    val ma = du.getAtkModel(0, i)

                    val lds = ma.shortPoint

                    val ldr = ma.longPoint - ma.shortPoint

                    val start = lds.coerceAtMost(lds + ldr)
                    val end = lds.coerceAtLeast(lds + ldr)

                    builder.append("$start ~ $end")

                    if(i < du.getAtkCount(0) - 1) {
                        builder.append(" / ")
                    }
                }

                return builder.toString()
            }
        }
    }

    fun getRange(em: Enemy?): String {
        if (em == null)
            return ""

        val tb = em.de.range

        if(!em.de.isLD && !em.de.isOmni)
            return tb.toString()

        if(em.id.pack != Identifier.DEF) {
            val de = em.de as CustomEntity

            val ma = if(de.getAtkCount(0) == 1 || allRangeSame(de)) {
                de.getAtkModel(0, 0)
            } else {
                de.repAtk
            }

            val lds = ma.shortPoint

            val ldr = ma.longPoint - ma.shortPoint

            val start = lds.coerceAtMost(lds + ldr)
            val end = lds.coerceAtLeast(lds + ldr)

            return "$tb | $start ~ $end"
        } else {
            val de = em.de as DataEnemy

            if(de.getAtkCount(0) == 0 || allRangeSame(de)) {
                val ma = de.getAtkModel(0, 0)

                val lds = ma.shortPoint

                val ldr = ma.longPoint - ma.shortPoint

                val start = lds.coerceAtMost(lds + ldr)
                val end = lds.coerceAtLeast(lds + ldr)

                return "$tb | $start ~ $end"
            } else {
                val builder = StringBuilder("$tb | ")

                for(i in 0 until de.getAtkCount(0)) {
                    val ma = de.getAtkModel(0, i)

                    val lds = ma.shortPoint

                    val ldr = ma.longPoint - ma.shortPoint

                    val start = lds.coerceAtMost(lds + ldr)
                    val end = lds.coerceAtLeast(lds + ldr)

                    builder.append("$start ~ $end")

                    if(i < de.getAtkCount(0) - 1) {
                        builder.append(" / ")
                    }
                }

                return builder.toString()
            }
        }
    }

    private fun allRangeSame(de: MaskEntity) : Boolean {
        val near = ArrayList<Int>()
        val far = ArrayList<Int>()

        for(atk in de.getAtks(0)) {
            near.add(atk.shortPoint)
            far.add(atk.longPoint)
        }

        if(near.isEmpty() && far.isEmpty()) {
            return true
        }

        for(n in near) {
            if(n != near[0]) {
                return false
            }
        }

        for(f in far) {
            if(f != far[0]) {
                return false
            }
        }

        return true
    }

    fun getCD(f: Form?, t: Treasure?, frse: Int, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        return if (frse == 0)
            t.getFinRes(du.respawn, 0).toString() + "f"
        else
            DecimalFormat("#.##").format(t.getFinRes(du.respawn, 0).toDouble() / 30) + "s"
    }

    fun getAtk(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        return if (du.getAtks(0).size > 1)
            getTotAtk(f, t, talent, lvs) + " " + getAtks(f, t, talent, lvs)
        else
            getTotAtk(f, t, talent, lvs)
    }

    fun getAtk(em: Enemy?, multi: Int): String {
        if (em == null)
            return ""

        return if (em.de.getAtks(0).size > 1)
            getTotAtk(em, multi) + " " + getAtks(em, multi)
        else
            getTotAtk(em, multi)
    }

    fun getSpd(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        return du.speed.toString()
    }

    fun getSpd(em: Enemy?): String {
        return em?.de?.speed?.toString() ?: ""
    }

    fun getBarrier(em: Enemy?): String {
        if (em == null)
            return ""

        return if (em.de.proc.BARRIER.health == 0)
            c.getString(R.string.unit_info_t_none)
        else
            em.de.proc.BARRIER.health.toString()
    }

    fun getHB(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        return du.hb.toString()
    }

    fun getHB(em: Enemy?): String {
        return em?.de?.hb?.toString() ?: ""
    }

    fun getHP(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        val result = if(f.du.pCoin != null && talent) {
            (((du.hp * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.defMulti).toInt() * (f.du.pCoin.getStatMultiplication(Data.PC2_HP, lvs.talents))).toInt()
        } else {
            ((du.hp * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.defMulti).toInt()
        }

        return result.toString()
    }

    fun getHP(em: Enemy?, multi: Int): String {
        if (em == null)
            return ""

        return (em.de.multi(BasisSet.current()) * em.de.hp * multi / 100).toInt().toString()
    }

    fun getTotAtk(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du else f.du

        val result: Int = if(f.du.pCoin != null && talent) {
            (((du.allAtk(0) * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt() * f.du.pCoin.getStatMultiplication(Data.PC2_ATK, lvs.talents)).toInt()
        } else {
            ((du.allAtk(0) * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt()
        }

        return result.toString()
    }

    private fun getTotAtk(em: Enemy?, multi: Int): String {
        if (em == null)
            return ""

        return (em.de.multi(BasisSet.current()) * em.de.allAtk(0) * multi / 100).toInt().toString()
    }

    fun getDPS(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        return if (f == null || t == null)
            ""
        else {
            val du = if(talent && f.du.pCoin != null)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du

            DecimalFormat("#.##").format(getTotAtk(f, t, talent, lvs).toDouble() / (du.getItv(0) / 30.0)).toString()
        }
    }

    fun getDPS(em: Enemy?, multi: Int): String {
        return if (em == null)
            ""
        else
            DecimalFormat("#.##").format(getTotAtk(em, multi).toDouble() / (em.de.getItv(0).toDouble() / 30)).toString()
    }

    fun getTrait(ef: Form?, talent: Boolean, lvs: Level, c: Context): String {
        if (ef == null) return ""

        val du: MaskUnit = if (ef.du.pCoin != null && talent)
            ef.du.pCoin.improve(lvs.talents)
        else
            ef.du

        var result: String

        result = Interpret.getTrait(du.traits, 0, c)

        if (result == "")
            result = c.getString(R.string.unit_info_t_none)

        if (result == allColor)
            result = c.getString(R.string.unit_info_t_allc)

        if (result == allTrait)
            result = c.getString(R.string.unit_info_t_allt)

        if (result.endsWith(", "))
            result = result.substring(0, result.length - 2)

        return result
    }

    fun getTrait(em: Enemy, c: Context): String {
        val de = em.de

        val allcolor = StringBuilder()
        val alltrait = StringBuilder()

        for (i in 0..8) {
            if (i != 0)
                allcolor.append(Interpret.TRAIT[i]).append(", ")

            alltrait.append(Interpret.TRAIT[i]).append(", ")
        }

        var result: String

        val star = de.star

        result = Interpret.getTrait(de.traits, star, c)

        if (result == "")
            result = c.getString(R.string.unit_info_t_none)

        if (result == allcolor.toString())
            result = c.getString(R.string.unit_info_t_allc)

        if (result == alltrait.toString())
            result = c.getString(R.string.unit_info_t_allt)

        if (result.endsWith(", "))
            result = result.substring(0, result.length - 2)

        return result
    }

    fun getCost(f: Form?, talent: Boolean, lvs: Level): String {
        if (f == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        return (du.price * 1.5).toInt().toString()
    }

    fun getDrop(em: Enemy?, t: Treasure): String {
        if (em == null)
            return ""

        return (em.de.drop * t.getDropMulti(0) / 100).toInt().toString()
    }

    private fun getAtks(f: Form?, t: Treasure?, talent: Boolean, lvs: Level): String {
        if (f == null || t == null)
            return ""

        val du: MaskUnit = if (f.du.pCoin != null)
            if (talent)
                f.du.pCoin.improve(lvs.talents)
            else
                f.du
        else
            f.du

        val atks = du.getAtks(0)

        val damges = ArrayList<Int>()

        for (atk in atks) {
            val result: Int = if(f.du.pCoin != null && talent) {
                (((atk.atk * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt() * f.du.pCoin.getStatMultiplication(Data.PC2_ATK, lvs.talents)).toInt()
            } else {
                ((atk.atk * f.unit.lv.getMult(lvs.lv + lvs.plusLv)).roundToInt() * t.atkMulti).toInt()
            }

            damges.add(result)
        }

        val result = StringBuilder("(")

        for (i in damges.indices) {
            if (i < damges.size - 1)
                result.append(damges[i]).append(", ")
            else
                result.append(damges[i]).append(")")
        }

        return result.toString()
    }

    private fun getAtks(em: Enemy?, multi: Int): String {
        if (em == null)
            return ""

        val atks = em.de.getAtks(0)

        val damages = ArrayList<Int>()

        for (atk in atks) {
            damages.add((atk.atk * em.de.multi(BasisSet.current()) * multi / 100).toInt())
        }

        val result = StringBuilder("(")

        for (i in damages.indices) {
            if (i < damages.size - 1)
                result.append("").append(damages[i]).append(", ")
            else
                result.append("").append(damages[i]).append(")")
        }
        return result.toString()
    }

    fun getSimu(f: Form?): String {
        if (f == null)
            return ""

        return if (Interpret.isType(f.du, 1))
            c.getString(R.string.sch_atk_ra)
        else
            c.getString(R.string.sch_atk_si)
    }

    fun getSimu(em: Enemy?): String {
        if (em == null)
            return ""

        return if (Interpret.isType(em.de, 1))
            c.getString(R.string.sch_atk_ra)
        else
            c.getString(R.string.sch_atk_si)
    }

    fun getTalentName(index: Int, f: Form, c: Context): String {
        val ans: String?

        val info = f.du.pCoin.info

        val trait = listOf(37, 38, 39, 40)
        val basic = listOf(25, 26, 27, 31, 32)

        if(talData[info[index][0]] == -1) {
            return "Invalid Data"
        }

        ans = when {
            trait.contains(info[index][0]) -> c.getString(R.string.talen_trait) + talTool[info[index][0]]
            basic.contains(info[index][0]) -> talTool[info[index][0]]
            f.du.pCoin.trait.isNotEmpty() && index == 0 -> {
                val tr = Interpret.getTrait(f.du.pCoin.trait, 0, c)

                if(tr.endsWith(", "))
                    c.getString(R.string.talen_abil) + tr.substring(0, tr.length - 2) + " " + talTool[info[index][0]]
                else
                    c.getString(R.string.talen_abil) + tr + " " + talTool[info[index][0]]
            }
            else -> c.getString(R.string.talen_abil) + " " + talTool[info[index][0]]
        }

        return ans
    }

    fun number(num: Int): String {
        return when (num) {
            in 0..9 -> {
                "00$num"
            }
            in 10..99 -> {
                "0$num"
            }
            else -> {
                num.toString()
            }
        }
    }

    fun getID(mapcode: String, stid: Int, posit: Int): String {
        return if(mapcode.length == 6) {
            val index = mapcodes.indexOf(mapcode)

            if(index != -1) {
                "${mapcolcid[index]}-$stid-$posit"
            } else {
                "$mapcode-$stid-$posit"
            }
        } else {
            "$stid=$posit"
        }
    }

    fun getDifficulty(diff: Int, ac: Activity): String {
        return if(diff < 0)
            ac.getString(R.string.unit_info_t_none)
        else
            "★$diff"
    }

    fun getEnergy(stage: Stage, c: Context) : String {
        val info = stage.info ?: return "0"

        return if (info is DefStageInfo) {
            if (stage.cont.cont.sid == "000014") {
                if (info.energy < 1000) {
                    c.getString(R.string.stg_info_catamina, info.energy)
                } else if(info.energy < 2000) {
                    c.getString(R.string.stg_info_cataminb, info.energy - 1000)
                } else {
                    c.getString(R.string.stg_info_cataminc, info.energy - 2000)
                }
            } else {
                info.energy.toString()
            }
        } else {
            "0"
        }
    }

    fun getLayer(data: SCDef.Line): String {
        return if (data.layer_0 == data.layer_1)
            "" + data.layer_0
        else
            data.layer_0.toString() + " ~ " + data.layer_1
    }

    fun getRespawn(data: SCDef.Line, frse: Boolean): String {
        return if (data.respawn_0 == data.respawn_1)
            if (frse)
                data.respawn_0.toString() + "f"
            else
                DecimalFormat("#.##").format(data.respawn_0.toFloat() / 30.toDouble()) + "s"
        else if (frse)
            data.respawn_0.toString() + "f ~ " + data.respawn_1 + "f"
        else
            DecimalFormat("#.##").format(data.respawn_0.toFloat() / 30.toDouble()) + "s ~ " + DecimalFormat("#.##").format(data.respawn_1.toFloat() / 30.toDouble()) + "s"
    }

    fun getBaseHealth(data: SCDef.Line): String {
        return data.castle_0.toString() + "%"
    }

    fun getMultiply(data: SCDef.Line, multi: Int): String {
        return if(data.multiple == data.mult_atk) {
            (data.multiple.toFloat() * multi.toFloat() / 100.toFloat()).toInt().toString() + "%"
        } else {
            (data.multiple.toFloat() * multi.toFloat() / 100.toFloat()).toInt().toString() + " / " + (data.mult_atk.toFloat() * multi.toFloat() / 100.toFloat()).toInt().toString() + "%"
        }
    }

    fun getNumber(data: SCDef.Line): String {
        return if (data.number == 0)
            c.getString(R.string.infinity)
        else
            data.number.toString()
    }

    fun getStart(data: SCDef.Line, frse: Boolean): String {
        return if (frse)
            data.spawn_0.toString() + "f"
        else
            DecimalFormat("#.##").format(data.spawn_0.toFloat() / 30.toDouble()) + "s"
    }

    fun getLimit(l: Limit?): Array<String> {
        if (l == null)
            return arrayOf("")

        val limits: MutableList<String> = ArrayList()

        if (l.line != 0) {
            val result = c.getString(R.string.limit_line) + " : " + c.getString(R.string.limit_line2)
            limits.add(result)
        }

        if (l.max != 0) {
            val result = c.getString(R.string.limit_max) + " : " + c.getString(R.string.limit_max2).replace("_", l.max.toString())
            limits.add(result)
        }

        if (l.min != 0) {
            val result = c.getString(R.string.limit_min) + " : " + c.getString(R.string.limit_min2).replace("_", l.min.toString())
            limits.add(result)
        }

        if (l.rare != 0) {
            val rid = intArrayOf(R.string.sch_rare_ba, R.string.sch_rare_ex, R.string.sch_rare_ra, R.string.sch_rare_sr, R.string.sch_rare_ur, R.string.sch_rare_lr)
            val rare = StringBuilder()

            for (i in rid.indices) {
                if (l.rare shr i and 1 == 1) {
                    rare.append(c.getString(rid[i])).append(", ")
                }
            }

            val result = c.getString(R.string.limit_rare) + " : " + rare.toString().substring(0, rare.length - 2)

            limits.add(result)
        }

        if (l.num != 0) {
            val result = c.getString(R.string.limit_deploy) + " : " + l.num
            limits.add(result)
        }

        if (l.group != null && l.group.fset.size != 0) {
            val units = StringBuilder()
            val u: List<Form> = ArrayList(l.group.fset)

            for (i in u.indices) {
                if (i == l.group.fset.size - 1) {
                    val f = MultiLangCont.get(u[i]) ?: u[i].names.toString()

                    units.append(f)
                } else {
                    val f = MultiLangCont.get(u[i]) ?: u[i].names.toString()

                    units.append(f).append(", ")
                }
            }

            val result: String = if (l.group.type == 0)
                c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra1).replace("_", units.toString())
            else
                c.getString(R.string.limit_chra) + " : " + c.getString(R.string.limit_chra2).replace("_", units.toString())

            limits.add(result)
        }
        return limits.toTypedArray()
    }

    fun getXP(xp: Int, t: Treasure?, legend: Boolean): String {
        if (t == null)
            return ""

        return if (legend)
            "" + (xp * t.xpMult * 9).toInt()
        else
            "" + (xp * t.xpMult).toInt()
    }

    fun getMiscellaneous(st: Stage) : ArrayList<String> {
        val res = ArrayList<String>()

        st.info ?: return res

        if(st.info !is DefStageInfo)
            return res

        if(st.cont.info.hiddenUponClear)
            res.add(c.getString(R.string.stg_info_hidden))

        if(st.cont.info.resetMode != -1) {
            when(st.cont.info.resetMode) {
                1 -> res.add(c.getString(R.string.stg_info_reset1))
                2 -> res.add(c.getString(R.string.stg_info_reset2))
                3 -> res.add(c.getString(R.string.stg_info_reset3))
                else -> res.add(c.getString(R.string.stg_info_reset).replace("_", st.cont.info.resetMode.toString()))
            }
        }

        if(st.cont.info.waitTime != -1) {
            res.add(c.getString(R.string.stg_info_wait).replace("_", st.cont.info.waitTime.toString()))
        }

        if(st.cont.info.clearLimit != -1) {
            res.add(c.getString(R.string.stg_info_numbplay).replace("_", st.cont.info.clearLimit.toString()))
        }

        if (st.cont.info.unskippable) {
            res.add(c.getString(R.string.stg_info_nocpu))
        }

        return res
    }
}