package com.yumetsuki.bcu.androidutil

import android.content.Context
import com.yumetsuki.bcu.androidutil.io.LangLoader
import common.pack.UserProfile

object Revalidater {
    fun validate(context: Context) {
        Definer.redefine(context)

        if (StaticStore.ludata.isNotEmpty()) {
            StaticStore.ludata.clear()

            val plist = UserProfile.getAllPacks()

            for(m in plist) {
                m ?: continue

                for(i in m.units.list.indices) {
                    val unit = m.units.list[i]

                    StaticStore.ludata.add(unit.id)
                }
            }
        }

        LangLoader.readUnitLang(context)
        LangLoader.readEnemyLang(context)
        LangLoader.readStageLang(context)

        if(StaticStore.medalnumber != 0) {
            LangLoader.readMedalLang(context)
        }
    }
}