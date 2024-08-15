package com.yumetsuki.bcu.androidutil.filter

import com.yumetsuki.bcu.androidutil.Interpret
import com.yumetsuki.bcu.androidutil.StatFilterElement
import com.yumetsuki.bcu.androidutil.StaticStore
import common.CommonStatic
import common.CommonStatic.Lang
import common.battle.data.MaskEntity
import common.pack.Identifier
import common.pack.SortedPackSet
import common.pack.UserProfile
import common.util.Data
import common.util.lang.MultiLangCont
import common.util.unit.AbEnemy
import common.util.unit.AbUnit
import common.util.unit.Trait
import common.util.unit.Unit
import java.util.Locale

object FilterEntity {
    @Synchronized
    fun setUnitFilter(pid: String): ArrayList<Identifier<AbUnit>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()
        val b5 = ArrayList<Boolean>()

        if (StaticStore.rare.isEmpty()) {
            for (i in p.units.list.indices)
                b0.add(true)
        }

        if (StaticStore.empty) {
            for (i in p.units.list.indices)
                b1.add(true)
        }

        if (StaticStore.attack.isEmpty()) {
            for (i in p.units.list.indices)
                b2.add(true)
        }

        if (StaticStore.tg.isEmpty()) {
            for (i in p.units.list.indices)
                b3.add(true)
        }

        if (StaticStore.ability.isEmpty()) {
            for (i in p.units.list.indices)
                b4.add(true)
        }

        if (StatFilterElement.statFilter.isEmpty()) {
            for(i in p.units.list.indices) {
                b5.add(true)
            }
        }

        for (u in p.units.list) {
            if(StaticStore.rare.isNotEmpty()) b0.add(StaticStore.rare.contains(u.rarity.toString()))

            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()
            val b50 = ArrayList<Boolean>()
            for (f in u.forms) {
                val du = if (StaticStore.talents) f.maxu() else f.du
                val t = du.traits
                val a = du.abi
                if (!StaticStore.empty)
                    if (StaticStore.atksimu)
                        b10.add(Interpret.isType(du, 1))
                    else
                        b10.add(Interpret.isType(du, 0))
                var b21 = !StaticStore.atkorand
                for (k in StaticStore.attack.indices) {
                    b21 = if (StaticStore.atkorand) b21 or Interpret.isType(du, StaticStore.attack[k].toInt()) else b21 and Interpret.isType(du, StaticStore.attack[k].toInt())
                }
                var b31 = !StaticStore.tgorand
                for (k in StaticStore.tg.indices) {
                    b31 = if (StaticStore.tgorand)
                        b31 or hasTrait(t, StaticStore.tg[k])
                    else
                        b31 and hasTrait(t, StaticStore.tg[k])
                }
                var b41 = !StaticStore.aborand
                for (k in StaticStore.ability.indices) {
                    val vect = StaticStore.ability[k]
                    if (vect[0] == 0) {
                        val bind = a and vect[1] != 0
                        b41 = if (StaticStore.aborand) b41 or bind else b41 and bind
                    } else if (vect[0] == 1) {
                        b41 = if (StaticStore.aborand) b41 or getChance(vect[1], du) else b41 and getChance(vect[1], du)
                    }
                }
                b20.add(b21)
                b30.add(b31)
                b40.add(b41)
                b50.add(StatFilterElement.performFilter(f, StatFilterElement.orand))
            }
            if (!StaticStore.empty) if (b10.contains(true)) b1.add(true) else b1.add(false)
            if (StaticStore.attack.isNotEmpty()) if (b20.contains(true)) b2.add(true) else b2.add(false)
            if (StaticStore.tg.isNotEmpty()) if (b30.contains(true)) b3.add(true) else b3.add(false)
            if (StaticStore.ability.isNotEmpty()) if (b40.contains(true)) b4.add(true) else b4.add(false)
            if (StatFilterElement.statFilter.isNotEmpty()) if (b50.contains(true)) b5.add(true) else b5.add(false)
        }

        val result = ArrayList<Identifier<AbUnit>>()

        val lang = Locale.getDefault().language

        for (i in p.units.list.indices) if (b0[i] && b1[i] && b2[i] && b3[i] && b4[i] && b5[i]) {
            val u = p.units.list[i]

            if (u.forms.any { f -> f.du == null || f.du.proc == null })
                continue

            if (StaticStore.entityname.isNotEmpty()) {
                var added = false

                for (j in u.forms.indices) {
                    if (added)
                        continue

                    var name = MultiLangCont.get(u.forms[j]) ?: u.forms[j].names.toString()

                    name = Data.trio(i) + " - " + name.lowercase()

                    added = if(CommonStatic.getConfig().langs[0].equals(Lang.Locale.KR) || lang == Interpret.KO) {
                        KoreanFilter.filter(name, StaticStore.entityname)
                    } else {
                        name.contains(StaticStore.entityname.lowercase())
                    }
                }

                if (added)
                    result.add(u.id)
            } else {
                result.add(u.id)
            }
        }

        return result
    }

    private fun getChance(data: Int, du: MaskEntity) : Boolean {
        return when(data) {
            in 0 until Data.PROC_TOT -> {
               du.proc.getArr(data).exists()
            }
            else -> false
        }
    }

    @Synchronized
    fun setEnemyFilter(pid: String): ArrayList<Identifier<AbEnemy>> {
        val p = UserProfile.getPack(pid) ?: return ArrayList()

        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()

        if (StaticStore.empty) {
            for (i in p.enemies.list.indices)
                b0.add(true)
        }

        if (StaticStore.attack.isEmpty())
            for (i in p.enemies.list.indices)
                b1.add(true)

        if (StaticStore.tg.isEmpty() && !StaticStore.starred)
            for (i in p.enemies.list.indices)
                b2.add(true)

        if (StaticStore.ability.isEmpty())
            for (i in p.enemies.list.indices)
                b3.add(true)

        if (StatFilterElement.statFilter.isEmpty())
            for(i in p.enemies.list.indices)
                b4.add(true)

        for (e in p.enemies.list) {
            var b10: Boolean
            var b20: Boolean
            var b30: Boolean

            val de = e.de
            val t = de.traits
            val a = de.abi

            if (!StaticStore.empty)
                if (StaticStore.atksimu)
                    b0.add(Interpret.isType(de, 1))
                else
                    b0.add(Interpret.isType(de, 0))

            b10 = !StaticStore.atkorand

            for (k in StaticStore.attack.indices) {
                b10 = if (StaticStore.atkorand)
                    b10 or Interpret.isType(de, StaticStore.attack[k].toInt())
                else
                    b10 and Interpret.isType(de, StaticStore.attack[k].toInt())
            }

            if (StaticStore.tg.isEmpty())
                b20 = true
            else {
                b20 = !StaticStore.tgorand
                for (k in StaticStore.tg.indices) {
                    b20 = if (StaticStore.tgorand)
                        b20 or hasTrait(t, StaticStore.tg[k])
                    else
                        b20 and hasTrait(t, StaticStore.tg[k])
                }
            }

            val b21 = de.star == 1

            b30 = !StaticStore.aborand

            for (k in StaticStore.ability.indices) {
                val vect = StaticStore.ability[k]

                if (vect[0] == 0) {
                    val bind = a and vect[1] != 0
                    b30 = if (StaticStore.aborand)
                        b30 or bind
                    else
                        b30 and bind
                } else if (vect[0] == 1) {
                    b30 = if (StaticStore.aborand)
                        b30 or getChance(vect[1], de)
                    else
                        b30 and getChance(vect[1], de)
                }
            }

            b1.add(b10)

            if (StaticStore.starred)
                b2.add(b20 && b21)
            else
                b2.add(b20)

            b3.add(b30)

            if(StatFilterElement.statFilter.isNotEmpty()) {
                b4.add(StatFilterElement.performFilter(e, StatFilterElement.orand))
            }
        }

        val result = ArrayList<Identifier<AbEnemy>>()

        val lang = Locale.getDefault().language

        println("B0 : ${b0.size} | B1 : ${b1.size} | B2 : ${b2.size} | B3 : ${b3.size} | B4 : ${b4.size} | ENEMY : ${p.enemies.size()}")

        for (i in p.enemies.list.indices)
            if (b0[i] && b1[i] && b2[i] && b3[i] && b4[i]) {
                val e = p.enemies.list[i]

                if (StaticStore.entityname.isNotEmpty()) {
                    var name = MultiLangCont.get(e) ?: e.names.toString()

                    name = Data.trio(i) + " - " + name.lowercase()

                    val added = if(CommonStatic.getConfig().langs[0].equals(Lang.Locale.KR) || lang == Interpret.KO) {
                        KoreanFilter.filter(name, StaticStore.entityname)
                    } else {
                        name.contains(StaticStore.entityname.lowercase())
                    }

                    if (added)
                        result.add(e.id)
                } else {
                    result.add(e.id)
                }
            }

        return result
    }

    @Synchronized
    fun setLuFilter() : ArrayList<Identifier<AbUnit>> {
        val b0 = ArrayList<Boolean>()
        val b1 = ArrayList<Boolean>()
        val b2 = ArrayList<Boolean>()
        val b3 = ArrayList<Boolean>()
        val b4 = ArrayList<Boolean>()
        val b5 = ArrayList<Boolean>()

        if(StaticStore.rare.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b0.add(true)
        }

        if(StaticStore.empty) {
            for(i in 0 until StaticStore.ludata.size)
                b1.add(true)
        }

        if(StaticStore.attack.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b2.add(true)
        }

        if(StaticStore.tg.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b3.add(true)
        }

        if(StaticStore.ability.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size)
                b4.add(true)
        }

        if (StatFilterElement.statFilter.isEmpty()) {
            for(i in 0 until StaticStore.ludata.size) {
                b5.add(true)
            }
        }

        for(info in StaticStore.ludata) {
            val u = try {
                Identifier.get(info)
            } catch (_: Exception) {
                continue
            }

            if(u !is Unit) {
                b0.add(false)
                b1.add(false)
                b2.add(false)
                b3.add(false)
                b4.add(false)
                continue
            }

            b0.add(StaticStore.rare.contains(u.rarity.toString()))

            val b10 = ArrayList<Boolean>()
            val b20 = ArrayList<Boolean>()
            val b30 = ArrayList<Boolean>()
            val b40 = ArrayList<Boolean>()
            val b50 = ArrayList<Boolean>()

            for(f in u.forms) {
                val du = if(StaticStore.talents)
                    f.maxu()
                else
                    f.du

                val t = du.traits
                val a = du.abi

                if(!StaticStore.empty) {
                    if(StaticStore.atksimu) {
                        b10.add(Interpret.isType(du, 1))
                    } else {
                        b10.add(Interpret.isType(du, 0))
                    }
                }

                var b21 = !StaticStore.atkorand

                for(k in StaticStore.attack.indices) {
                    b21 = if(StaticStore.atkorand) {
                        b21 or Interpret.isType(du, StaticStore.attack[k].toInt())
                    } else {
                        b21 and Interpret.isType(du, StaticStore.attack[k].toInt())
                    }
                }

                var b31 = !StaticStore.tgorand

                for(k in StaticStore.tg.indices) {
                    b31 = if(StaticStore.tgorand) {
                        b31 or hasTrait(t, StaticStore.tg[k])
                    } else {
                        b31 and hasTrait(t, StaticStore.tg[k])
                    }
                }

                var b41 = !StaticStore.aborand

                for(k in StaticStore.ability.indices) {
                    val vect = StaticStore.ability[k]

                    if(vect[0] == 0) {
                        val bind = a and vect[1] != 0
                        b41 = if(StaticStore.aborand) {
                            b41 or bind
                        } else {
                            b41 and bind
                        }
                    } else if (vect[0] == 1) {
                        b41 = if(StaticStore.aborand) {
                            b41 or getChance(vect[1], du)
                        } else {
                            b41 and getChance(vect[1], du)
                        }
                    }
                }

                b20.add(b21)
                b30.add(b31)
                b40.add(b41)
                b50.add(StatFilterElement.performFilter(f, StatFilterElement.orand))
            }

            b1.add(!StaticStore.empty && b10.contains(true))
            b2.add(b20.contains(true))
            b3.add(b30.contains(true))
            b4.add(b40.contains(true))
            b5.add(b50.contains(true))
        }

        val result = ArrayList<Identifier<AbUnit>>()

        val lang = Locale.getDefault().language

        for(i in StaticStore.ludata.indices) {
            if(b0[i] && b1[i] && b2[i] && b3[i] && b4[i] && b5[i]) {
                val u = Identifier.get(StaticStore.ludata[i]) ?: continue

                if (u.forms.any { f -> f.du == null || f.du.proc == null })
                    continue

                if(StaticStore.entityname.isNotEmpty()) {
                    var added = false

                    for(j in u.forms.indices) {
                        if(added)
                            continue

                        var name = MultiLangCont.get(u.forms[j]) ?: u.forms[j].names.toString()

                        name = Data.trio(u.id.id) + " - " + name.lowercase()

                        added = if(CommonStatic.getConfig().langs[0].equals(Lang.Locale.KR) || lang == Interpret.KO) {
                            KoreanFilter.filter(name, StaticStore.entityname)
                        } else {
                            name.contains(StaticStore.entityname.lowercase())
                        }
                    }

                    if(added)
                        result.add(u.id)
                } else {
                    result.add(u.id)
                }
            }
        }

        return result
    }

    private fun hasTrait(traits: SortedPackSet<Trait>, t: Identifier<Trait>) : Boolean {
        for(tr in traits) {
            if(tr.id.equals(t))
                return true
        }

        return false
    }
}