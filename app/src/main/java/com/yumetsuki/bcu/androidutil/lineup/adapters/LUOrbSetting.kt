package com.yumetsuki.bcu.androidutil.lineup.adapters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.Interpret
import com.yumetsuki.bcu.androidutil.StaticStore
import com.yumetsuki.bcu.androidutil.io.ErrorLogWriter
import com.yumetsuki.bcu.androidutil.lineup.LineUpView
import com.yumetsuki.bcu.androidutil.supports.SingleClick
import common.CommonStatic
import common.battle.BasisSet
import common.battle.data.OrbInfo
import common.util.Data
import common.util.unit.Form
import common.util.unit.Level
import common.util.unit.Trait

class LUOrbSetting : Fragment() {
    companion object {
        fun newInstance(line: LineUpView) : LUOrbSetting {
            val o = LUOrbSetting()
            o.setLineup(line)

            return o
        }
    }

    private lateinit var f: Form
    private var orb = ArrayList<IntArray>()
    private lateinit var line: LineUpView

    private var isUpdating = false

    private val obj = Object()

    private val traits = intArrayOf(
        R.string.sch_red, R.string.sch_fl, R.string.sch_bla, R.string.sch_me, R.string.sch_an,
        R.string.sch_al, R.string.sch_zo, R.string.sch_re, R.string.sch_wh, R.string.esch_witch,
        R.string.esch_eva, R.string.sch_de, R.string.eff_none
    )
    private val grades = arrayOf("D", "C", "B", "A", "S")

    private val traitData = ArrayList<Int>()
    private val typeData = ArrayList<Byte>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.lineup_orb_setting, container, false)
        if (context == null) return v

        val obj = Object()

        val const = v.findViewById<ConstraintLayout>(R.id.orbconst)

        const.visibility = View.INVISIBLE

        update(v)

        synchronized(obj) {
            while(isUpdating) {
                try{
                    obj.wait()
                } catch (e: InterruptedException) {
                    ErrorLogWriter.writeLog(e)
                }
            }
        }

        const.visibility = View.VISIBLE

        return v
    }

    fun update() {
        val v = view ?: return
        if (context == null) return

        val obj = Object()

        val const = v.findViewById<ConstraintLayout>(R.id.orbconst)

        const.visibility = View.INVISIBLE

        update(v)

        synchronized(obj) {
            while(isUpdating) {
                try{
                    obj.wait()
                } catch (e: InterruptedException) {
                    ErrorLogWriter.writeLog(e)
                }
            }
        }

        const.visibility = View.VISIBLE
    }

    private fun listeners(v: View) {
        val orbs = v.findViewById<Spinner>(R.id.orbspinner)
        val type = v.findViewById<Spinner>(R.id.orbtype)
        val trait = v.findViewById<Spinner>(R.id.orbtrait)
        val grade = v.findViewById<Spinner>(R.id.orbgrade)
        val add = v.findViewById<FloatingActionButton>(R.id.orbadd)
        val remove = v.findViewById<FloatingActionButton>(R.id.orbremove)
        val image = v.findViewById<ImageView>(R.id.orbimage)
        val desc = v.findViewById<TextView>(R.id.orbdesc)

        val o = f.orbs ?: return

        val c = context ?: return

        val slot = o.slots != -1

        orbs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position >= orb.size) {
                    return
                }

                val l = BasisSet.current().sele.lu.getLv(f)

                updateSpinners(type, trait, grade, slot, orb[position], l)
                generateImage(orb[position], image)

                if(orb[position].isNotEmpty() && orb[position][0] == 0) {
                    val orbMap = CommonStatic.getBCAssets().ORB[orb[position][Data.ORB_TYPE.toInt()].toByte()] ?: return

                    generateTraitData(orbMap, slot, f, l, needTraitFiltering(orb[position]))
                }
            }
        }

        add.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(!slot) {
                    orb.add(intArrayOf(0, Data.TB_RED.toInt(), 0))

                    setData()

                    val a0 = object : ArrayAdapter<String>(c, R.layout.spinnersmall, generateOrbTexts().toTypedArray()){
                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val vi = super.getDropDownView(position, convertView, parent)

                            (vi as TextView).text = generateOrbTextAt(position)

                            return vi
                        }
                    }

                    orbs.adapter = a0
                    orbs.setSelection(orb.size-1)

                    if(orb.isNotEmpty()) {
                        remove.show()
                        setAppear(image, orbs, type, trait, grade)
                    } else {
                        remove.hide()
                    }

                    setLevel()
                    updateDescription(orb[orbs.selectedItemPosition], desc)
                }
            }

        })

        remove.setOnClickListener(object : SingleClick() {
            override fun onSingleClick(v: View?) {
                if(!slot && orb.isNotEmpty()) {
                    val p = orbs.selectedItemPosition

                    orb.removeAt(orbs.selectedItemPosition)

                    setData()

                    val a0 = object : ArrayAdapter<String>(c, R.layout.spinnersmall, generateOrbTexts().toTypedArray()) {
                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val vi = super.getDropDownView(position, convertView, parent)

                            (vi as TextView).text = generateOrbTextAt(position)

                            return vi
                        }
                    }

                    orbs.adapter = a0

                    if(p >= orb.size) {
                        orbs.setSelection(orb.size-1)
                    } else {
                        orbs.setSelection(p)
                    }

                    setLevel()

                    if(orb.isEmpty()) {
                        remove.hide()
                        setDisappear(image, type, trait, grade, orbs)
                        updateDescription(intArrayOf(), desc)
                    } else {
                        remove.show()
                        updateDescription(orb[orbs.selectedItemPosition], desc)
                    }
                }
            }

        })

        type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(orbs.selectedItemPosition >= orb.size || orbs.selectedItemPosition < 0) {
                    return
                }

                var data = orb[orbs.selectedItemPosition]

                if(slot) {
                    if(position == 0) {
                        orb[orbs.selectedItemPosition] = intArrayOf()

                        setLevel()
                        updateDescription(orb[orbs.selectedItemPosition], desc)

                        setDisappear(image, trait, grade)

                        val text = orbs.getChildAt(0) ?: return

                        (text as TextView).text = generateOrbTextAt(orbs.selectedItemPosition)

                        return
                    } else {
                        if(data.isEmpty()) {
                            orb[orbs.selectedItemPosition] = intArrayOf(0, Data.TB_RED.toInt(), 0)
                            data = orb[orbs.selectedItemPosition]
                        }

                        data[0] = typeData[position-1].toInt()

                        trait.isEnabled = true
                        grade.isEnabled = true

                        orb[orbs.selectedItemPosition] = data
                    }
                } else {
                    data[0] = typeData[position].toInt()
                }

                setAppear(image, trait, grade)

                val od = CommonStatic.getBCAssets().ORB[data[Data.ORB_TYPE.toInt()].toByte()] ?: return
                val l = BasisSet.current().sele.lu.getLv(f)

                generateTraitData(od, slot, f, l, needTraitFiltering(data))

                val t = ArrayList<String>()

                val traitList = ArrayList<Trait>()

                if (slot) {
                    val mu = if (f.du.pCoin != null) {
                        f.du.pCoin.improve(l.talents) ?: return
                    } else
                        f.du ?: return

                    for(tr in mu.traits)
                        if(tr.BCTrait())
                            traitList.add(tr)
                } else {
                    for(form in f.unit.forms) {
                        val mu = if (f.du.pCoin != null) {
                            f.du.pCoin.improve(l.talents) ?: return
                        } else
                            f.du ?: return

                        for(tr in mu.traits)
                            if(tr.BCTrait() && !traitList.contains(tr))
                                traitList.add(tr)
                    }
                }

                if(needTraitFiltering(data)) {
                    for(tr in traitList) {
                        if(od.contains(1 shl tr.id.id)) {
                            val r = getTextIndex(1 shl tr.id.id)

                            if(r >= traits.size || r < 0) {
                                Log.e("LUOrbSetting", "Invalid trait data in updateSpinners() : ${tr.id.id}")
                                return
                            } else {
                                t.add(c.getString(traits[r]))
                            }
                        }
                    }
                }

                if(t.isEmpty()) {
                    for(i in od.keys) {
                        val r = getTextIndex(i)

                        if(r >= traits.size) {
                            Log.e("LUOrbSetting", "Invalid trait data in updateSpinners() : $i")
                            return
                        } else if (r == -1)
                            t.add(c.getString(traits[traits.size - 1]))
                        else
                            t.add(c.getString(traits[r]))
                    }
                }

                val a1 = ArrayAdapter(c, R.layout.spinnersmall, t.toTypedArray())

                trait.adapter = a1

                trait.setSelection(traitData.indexOf(data[1]), false)

                val g = od[data[1]] ?: return

                val gr = ArrayList<String>()

                for(i in g) {
                    if(i >= grades.size) {
                        Log.e("LUOrbSetting", "Invalid grade data int updateSpinners() : $i")
                        return
                    } else {
                        gr.add(grades[i.toInt()])
                    }
                }

                val a2 = ArrayAdapter(c, R.layout.spinnersmall, gr.toTypedArray())

                grade.adapter = a2

                grade.setSelection(data[2], false)

                orb[orbs.selectedItemPosition] = data

                setLevel()
                updateDescription(orb[orbs.selectedItemPosition], desc)

                generateImage(orb[orbs.selectedItemPosition], image)

                val text = orbs.getChildAt(0) ?: return

                (text as TextView).text = generateOrbTextAt(orbs.selectedItemPosition)
            }
        }

        trait.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(orbs.selectedItemPosition >= orb.size || orbs.selectedItemPosition < 0) {
                    return
                }

                val data = orb[orbs.selectedItemPosition]

                if(data.isEmpty()) {
                    return
                }

                val od = CommonStatic.getBCAssets().ORB[data[Data.ORB_TYPE.toInt()].toByte()] ?: return

                if(position >= traitData.size) {
                    return
                }

                data[1] = traitData[position]

                orb[orbs.selectedItemPosition] = data

                val g = od[data[1]] ?: return

                val gr = ArrayList<String>()

                for(i in g) {
                    if(i >= grades.size) {
                        Log.e("LUOrbSetting", "Invalid grade data int updateSpinners() : $i")
                        return
                    } else {
                        gr.add(grades[i.toInt()])
                    }
                }

                val a2 = ArrayAdapter(c, R.layout.spinnersmall, gr.toTypedArray())

                grade.adapter = a2

                grade.setSelection(data[2], false)

                generateImage(orb[orbs.selectedItemPosition], image)

                val text = orbs.getChildAt(0) ?: return

                (text as TextView).text = generateOrbTextAt(orbs.selectedItemPosition)

                setLevel()
                updateDescription(orb[orbs.selectedItemPosition], desc)
            }
        }

        grade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(orbs.selectedItemPosition >= orb.size || orbs.selectedItemPosition < 0) {
                    return
                }

                val data = orb[orbs.selectedItemPosition]

                if(data.isEmpty())
                    return

                data[2] = position

                orb[orbs.selectedItemPosition] = data

                setLevel()
                updateDescription(orb[orbs.selectedItemPosition], desc)

                generateImage(orb[orbs.selectedItemPosition], image)

                val text = orbs.getChildAt(0) ?: return

                (text as TextView).text = generateOrbTextAt(orbs.selectedItemPosition)
            }
        }
    }

    private fun update(v: View) {
        if (!this::line.isInitialized)
            return

        val temporaryForm = if (StaticStore.position[0] == -1)
            return
        else if (StaticStore.position[0] == LineUpView.REPLACE)
            line.repform
        else {
            line.lu.fs[StaticStore.position[0]][StaticStore.position[1]]
        }

        val orbs = v.findViewById<Spinner>(R.id.orbspinner)
        val type = v.findViewById<Spinner>(R.id.orbtype)
        val trait = v.findViewById<Spinner>(R.id.orbtrait)
        val grade = v.findViewById<Spinner>(R.id.orbgrade)
        val add = v.findViewById<FloatingActionButton>(R.id.orbadd)
        val remove = v.findViewById<FloatingActionButton>(R.id.orbremove)
        val image = v.findViewById<ImageView>(R.id.orbimage)
        val desc = v.findViewById<TextView>(R.id.orbdesc)

        if (temporaryForm !is Form) {
            setDisappear(orbs, type, trait, grade, add, remove, image, desc)

            synchronized(obj) {
                isUpdating = false
                obj.notifyAll()
            }

            return
        } else {
            f = temporaryForm
        }

        updateOrbData()

        if(f.orbs == null) {
            setDisappear(orbs, type, trait, grade, add, remove, image, desc)

            synchronized(obj) {
                isUpdating = false
                obj.notifyAll()
            }

            return
        } else {
            setAppear(orbs, type)
        }

        val l = BasisSet.current().sele.lu.getLv(f)

        val o = f.orbs

        if(o == null) {
            synchronized(obj) {
                isUpdating = false
                obj.notifyAll()
            }

            return
        }

        if(l == null) {
            synchronized(obj) {
                isUpdating = false
                obj.notifyAll()
            }

            return
        }

        if(l.orbs == null && f.orbs != null) {
            if(f.orbs.slots != -1) {
                l.orbs = Array(f.orbs.slots) { intArrayOf()}
            } else {
                l.orbs = Array(0) { intArrayOf()}
            }
        }

        parseData()

        val c = context

        if(c == null) {
            synchronized(obj) {
                isUpdating = false
                obj.notifyAll()
            }

            return
        }

        val result = generateOrbTexts()

        val oa = object : ArrayAdapter<String>(c, R.layout.spinnersmall, result.toTypedArray()) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val vi = super.getDropDownView(position, convertView, parent)

                (vi as TextView).text = generateOrbTextAt(position)

                if(CommonStatic.getConfig().realLevel && f.orbs != null && f.orbs.slots != -1 && f.orbs.limits[position] == 1 && l.lv + l.plusLv < 60)
                    vi.setTextColor(vi.textColors.withAlpha(64))

                return vi
            }

            override fun isEnabled(position: Int): Boolean {
                return f.orbs == null || f.orbs.slots == -1 || !CommonStatic.getConfig().realLevel || f.orbs.limits[position] != 1 || l.lv + l.plusLv >= 60
            }
        }

        orbs.adapter = oa

        if(orb.isNotEmpty()) {
            orbs.setSelection(0)
        } else {
            val types = ArrayList<String>()

            if(f.orbs.slots != -1) {
                types.add(c.getString(R.string.unit_info_t_none))
                types.add(c.getString(R.string.orb_atk))
                types.add(c.getString(R.string.orb_def))
            } else {
                types.add(c.getString(R.string.orb_atk))
                types.add(c.getString(R.string.orb_def))
            }

            val a0 = ArrayAdapter(c, R.layout.spinnersmall, types.toTypedArray())

            type.adapter = a0
        }

        if(o.slots == -1) {
            if(l.orbs.isEmpty()) {
                setAppear(orbs, add)
                setDisappear(type, trait, grade, image, remove)
            } else {
                setAppear(type, trait, grade, image, remove, orbs, add)
            }
        } else {
            if(l.orbs[0].isEmpty()) {
                setAppear(type, orbs)
                setDisappear(grade, trait, add, remove, image)
            } else {
                setDisappear(add, remove)
                setAppear(type, orbs, trait, grade, image)
            }
        }

        listeners(v)

        synchronized(obj) {
            isUpdating = false
            obj.notifyAll()
        }
    }

    private fun parseData() {
        orb.clear()

        val data = BasisSet.current().sele.lu.getLv(f).orbs ?: return

        for(d in data) {
            orb.add(d)
        }
    }

    private fun updateSpinners(type: Spinner, trait: Spinner, grade: Spinner, slot: Boolean, data: IntArray, lv: Level) {
        if(!this::f.isInitialized)
            return

        val c = context ?: return

        val u = f.unit ?: return

        var str = false
        var mas = false
        var res = false

        val traitList = ArrayList<Trait>()

        if (slot) {
            val mu = if (f.du.pCoin != null) {
                f.du.pCoin.improve(lv.talents) ?: return
            } else
                f.du ?: return

            str = (mu.proc.DMGINC.mult > 100 && mu.proc.DMGINC.mult < 300) || (mu.proc.DEFINC.mult > 100 && mu.proc.DEFINC.mult < 400)
            mas = mu.proc.DMGINC.mult >= 300
            res = mu.proc.DEFINC.mult >= 400

            for(t in mu.traits)
                if(t.BCTrait())
                    traitList.add(t)
        } else {
            for(form in u.forms) {
                val mu = if (f.du.pCoin != null) {
                    f.du.pCoin.improve(lv.talents) ?: return
                } else
                    f.du ?: return

                str = str or ((mu.proc.DMGINC.mult > 100 && mu.proc.DMGINC.mult < 300) || (mu.proc.DEFINC.mult > 100 && mu.proc.DEFINC.mult < 400))
                mas = mas or (mu.proc.DMGINC.mult >= 300)
                res = res or (mu.proc.DEFINC.mult >= 400)

                for(t in mu.traits)
                    if(t.BCTrait() && !traitList.contains(t))
                        traitList.add(t)
            }
        }

        val types = ArrayList<String>()

        if(slot) {
            types.add(c.getString(R.string.unit_info_t_none))
        }

        typeData.clear()

        types.add(c.getString(R.string.orb_atk))
        typeData.add(Data.ORB_ATK)
        types.add(c.getString(R.string.orb_def))
        typeData.add(Data.ORB_RES)

        if(str) {
            types.add(c.getString(R.string.orb_str))
            typeData.add(Data.ORB_STRONG)
        }
        if(mas) {
            types.add(c.getString(R.string.orb_mas))
            typeData.add(Data.ORB_MASSIVE)
        }
        if(res) {
            types.add(c.getString(R.string.orb_res))
            typeData.add(Data.ORB_RESISTANT)
        }
        for (i in Data.ORB_MINIDEATHSURGE until Data.ORB_TYPE_TOTAL)
            if (data.isEmpty() || !OrbInfo.onlyOne(i) || data[Data.ORB_TYPE.toInt()] == i || !lv.equippingOrb(i)) {
                types.add(getType(i))
                typeData.add(i.toByte())
            }
        val a0 = ArrayAdapter(c, R.layout.spinnersmall, types.toTypedArray())
        type.adapter = a0

        if(data.isEmpty()) {
            if(slot) {
                type.setSelection(0, false)

                setDisappear(trait, grade)

                trait.isEnabled = false
                grade.isEnabled = false

                return
            } else {
                Log.e("LUOrbSetting", "Invalid format detected in updateSpinners() ! : ${data.contentToString()}")
                return
            }
        } else {
            trait.isEnabled = true
            grade.isEnabled = true

            if(slot) {
                type.setSelection(typeData.indexOf(data[Data.ORB_TYPE.toInt()].toByte())+1, false)
            } else
                type.setSelection(typeData.indexOf(data[Data.ORB_TYPE.toInt()].toByte()), false)
        }

        setAppear(trait, grade)

        val od = CommonStatic.getBCAssets().ORB[data[Data.ORB_TYPE.toInt()].toByte()] ?: return
        val t = ArrayList<String>()

        if(needTraitFiltering(data)) {
            for(tr in traitList) {
                if(od.contains(1 shl tr.id.id)) {
                    val r = getTextIndex(1 shl tr.id.id)

                    if(r >= traits.size) {
                        Log.e("LUOrbSetting", "Invalid trait data in updateSpinners() : ${tr.id.id}")
                        return
                    } else {
                        t.add(c.getString(traits[r]))
                    }
                }
            }
        }

        if(t.isEmpty()) {
            for(i in od.keys) {
                val r = getTextIndex(i)

                if(r >= traits.size || r < 0) {
                    Log.e("LUOrbSetting", "Invalid trait data in updateSpinners() : $i")
                    return
                } else {
                    t.add(c.getString(traits[r]))
                }
            }
        }

        val a1 = ArrayAdapter(c, R.layout.spinnersmall, t.toTypedArray())

        trait.adapter = a1

        generateTraitData(od, slot, f, lv, needTraitFiltering(data))

        trait.setSelection(traitData.indexOf(data[Data.ORB_TRAIT.toInt()]), false)

        val g = od[data[Data.ORB_TRAIT.toInt()]] ?: return

        val gr = ArrayList<String>()

        for(i in g) {
            if(i >= grades.size) {
                Log.e("LUOrbSetting", "Invalid grade data int updateSpinners() : $i")
                return
            } else
                gr.add(grades[i.toInt()])
        }

        val a2 = ArrayAdapter(c, R.layout.spinnersmall, gr.toTypedArray())

        grade.adapter = a2

        grade.setSelection(data[Data.ORB_GRADE.toInt()], false)
    }

    private fun generateOrbTexts() : List<String> {
        val res = ArrayList<String>()

        val c = context ?: return res

        val o = f.orbs ?: return res

        if(o.slots != -1) {
            val l = BasisSet.current().sele.lu.getLv(f)

            if(l.orbs == null) {
                for(i in 0 until o.slots) {
                    res.add(c.getString(R.string.lineup_orb)+" ${i+1} - "+c.getString(R.string.unit_info_t_none))
                }

                return res
            }

            for(i in l.orbs.indices) {
                val data = l.orbs[i]

                if(data.isEmpty()) {
                    res.add(c.getString(R.string.lineup_orb)+" ${i+1} - "+c.getString(R.string.unit_info_t_none))
                } else {
                    res.add(c.getString(R.string.lineup_orb)+" ${i+1} - {${getType(data[Data.ORB_TYPE.toInt()])}, ${getTrait(data[Data.ORB_TRAIT.toInt()])}, ${getGrade(data[Data.ORB_GRADE.toInt()])}}")
                }
            }
        } else {
            val l = BasisSet.current().sele.lu.getLv(f) ?: return res

            if(l.orbs == null || l.orbs.isEmpty()) {
                return res
            } else {
                for(i in l.orbs.indices) {
                    val data = l.orbs[i]

                    if(data.isEmpty()) {
                        Log.e("LUOrbSetting","Invalid format detected in generateOrbTexts() ! : ${l.orbs.contentDeepToString()}")
                        return res
                    } else {
                        res.add(c.getString(R.string.lineup_orb)+" ${i+1} - {${getType(data[Data.ORB_TYPE.toInt()])}, ${getTrait(data[Data.ORB_TRAIT.toInt()])}, ${getGrade(data[Data.ORB_GRADE.toInt()])}}")
                    }
                }
            }
        }

        return res
    }

    private fun generateOrbTextAt(index: Int) : String {
        val c = context ?: return ""

        val o = f.orbs ?: return ""

        if(o.slots != -1) {
            val l = BasisSet.current().sele.lu.getLv(f)

            if(l.orbs == null) {
                return c.getString(R.string.lineup_orb)+" ${index+1} - "+c.getString(R.string.unit_info_t_none)
            }

            if(index >= l.orbs.size)
                return ""

            val data = orb[index]

            return if(data.isEmpty()) {
                c.getString(R.string.lineup_orb)+" ${index+1} - "+c.getString(R.string.unit_info_t_none)
            } else {
                c.getString(R.string.lineup_orb)+" ${index+1} - {${getType(data[Data.ORB_TYPE.toInt()])}, ${getTrait(data[Data.ORB_TRAIT.toInt()])}, ${getGrade(data[Data.ORB_GRADE.toInt()])}}"
            }
        } else {
            val l = BasisSet.current().sele.lu.getLv(f) ?: return ""

            return if(l.orbs == null || l.orbs.isEmpty()) {
                ""
            } else {
                val data = orb[index]

                if(data.isEmpty()) {
                    Log.e("LUOrbSetting","Invalid format detected in generateOrbTexts() ! : ${l.orbs.contentDeepToString()}")
                    ""
                } else {
                    c.getString(R.string.lineup_orb)+"${index+1} - {${getType(data[Data.ORB_TYPE.toInt()])}, ${getTrait(data[Data.ORB_TRAIT.toInt()])}, ${getGrade(data[Data.ORB_GRADE.toInt()])}}"
                }
            }
        }
    }

    fun setLineup(line: LineUpView) {
        this.line = line
    }

    private fun getType(type: Int) : String {
        val c = context ?: return "Unknown Type $type"

        return when(type.toByte()) {
            Data.ORB_ATK -> c.getString(R.string.orb_atk)
            Data.ORB_RES -> c.getString(R.string.orb_def)
            Data.ORB_STRONG -> c.getString(R.string.orb_str)
            Data.ORB_MASSIVE -> c.getString(R.string.orb_mas)
            Data.ORB_RESISTANT -> c.getString(R.string.orb_res)
            Data.ORB_MINIDEATHSURGE -> c.getString(R.string.orb_mds)
            Data.ORB_RESWAVE -> c.getString(R.string.orb_rsw)
            Data.ORB_REFUND -> c.getString(R.string.orb_refn)
            Data.ORB_RESKB -> c.getString(R.string.orb_rkb)
            Data.ORB_SOLBUFF -> c.getString(R.string.orb_sol)
            Data.ORB_BAKILL -> c.getString(R.string.orb_colo)
            else -> return "Unknown Type $type"
        }
    }

    private fun getTrait(trait: Int) : String {
        val r = StringBuilder()

        for(i in Interpret.TRAIT.indices) {
            if(((trait shr i) and 1) > 0) {
                r.append(getString(Interpret.TRAIT[i])).append("/ ")
            }
        }

        var res = r.toString()

        if(res.endsWith("/ ")) {
            res = res.substring(0, r.length - 2)
        }

        return res
    }

    private fun getGrade(grade: Int) : String {
        return when(grade) {
            0 -> "D"
            1 -> "C"
            2 -> "B"
            3 -> "A"
            4 -> "S"
            else -> "Unknown Grade $grade"
        }
    }

    private fun setData() {
        val o = BasisSet.current().sele.lu.getLv(f) ?: return

        o.orbs = orb.toTypedArray()
    }

    private fun setAppear(vararg  view: View) {
        for(v in view) {
            if(v is FloatingActionButton) {
                if(v.isOrWillBeHidden)
                    v.show()
            } else {
                if(v.visibility == View.GONE || v.visibility == View.INVISIBLE)
                    v.visibility = View.VISIBLE
            }
        }
    }

    private fun generateImage(data: IntArray?, v: ImageView) {
        val c = context ?: return

        if(data == null || data.isEmpty()) {
            v.visibility = View.INVISIBLE
            return
        } else {
            v.visibility = View.VISIBLE
        }

        val b = StaticStore.empty(c, 96f, 96f)

        val cv = Canvas(b)

        val p = Paint()
        val ind = if (getTextIndex(data[Data.ORB_TRAIT.toInt()]) != -1) getTextIndex(data[Data.ORB_TRAIT.toInt()]) else CommonStatic.getBCAssets().TRAITS.size - 1
        cv.drawBitmap(StaticStore.getResizeb(CommonStatic.getBCAssets().TRAITS[0][ind].bimg() as Bitmap, c, 96f), 0f, 0f, p)

        p.alpha = (255 * 0.75).toInt()

        cv.drawBitmap(StaticStore.getResizeb(CommonStatic.getBCAssets().TYPES[0][data[Data.ORB_TYPE.toInt()]].bimg() as Bitmap, c, 96f), 0f, 0f, p)

        p.alpha = 255

        cv.drawBitmap(StaticStore.getResizeb(CommonStatic.getBCAssets().GRADES[data[Data.ORB_GRADE.toInt()]].bimg() as Bitmap, c, 96f), 0f, 0f, p)

        v.setImageBitmap(b)
    }

    private fun generateTraitData(data: Map<Int, List<Byte>>, slot: Boolean, f: Form, lv: Level, filter: Boolean) {
        val traitList = ArrayList<Trait>()

        if (slot) {
            val mu = if (f.du.pCoin != null) {
                f.du.pCoin.improve(lv.talents) ?: return
            } else {
                f.du ?: return
            }

            for(t in mu.traits) {
                if(t.BCTrait())
                    traitList.add(t)
            }
        } else {
            for(form in f.unit.forms) {
                val mu = if (f.du.pCoin != null) {
                    f.du.pCoin.improve(lv.talents) ?: return
                } else {
                    f.du ?: return
                }

                for(t in mu.traits) {
                    if(t.BCTrait() && !traitList.contains(t))
                        traitList.add(t)
                }
            }
        }

        traitData.clear()

        if(filter) {
            for(t in traitList) {
                if (data.containsKey(1 shl t.id.id))
                    traitData.add(1 shl t.id.id)
            }
        }

        if(traitData.isEmpty()) {
            for(t in data.keys) {
                traitData.add(t)
            }
        }
    }

    private fun setDisappear(vararg view: View) {
        for(v in view) {
            when (v) {
                is FloatingActionButton -> {
                    if(v.isOrWillBeShown) {
                        v.hide()
                    }
                }
                is ImageView -> {
                    if(v.visibility == View.VISIBLE) {
                        v.visibility = View.INVISIBLE
                    }
                }
                is Spinner -> {
                    if(v.visibility == View.VISIBLE) {
                        v.visibility = View.INVISIBLE
                    }
                }
                else -> {
                    if(v.visibility == View.VISIBLE) {
                        v.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setLevel() {
        val o = Array(orb.size) { i -> orb[i]}

        val l = BasisSet.current().sele.lu.getLv(f) ?: return

        l.orbs = o

        val c = context ?: return

        StaticStore.saveLineUp(c, false)
    }

    private fun updateDescription(data: IntArray, text: TextView) {
        val c = context ?: return

        if(data.isEmpty()) {
            text.text = null
            return
        }

        val grade = data[Data.ORB_GRADE.toInt()].toByte()
        val s = when(data[Data.ORB_TYPE.toInt()].toByte()) {
            Data.ORB_ATK -> c.getString(R.string.orb_atk_desc)
                .replace("_",OrbInfo.get(Data.ORB_ATK,grade)[0].toString())
            Data.ORB_RES -> c.getString(R.string.orb_def_desc)
                .replace("_",OrbInfo.get(Data.ORB_RES,grade)[0].toString())
            Data.ORB_STRONG -> c.getString(R.string.orb_str_desc)
                .replace("_", (OrbInfo.get(Data.ORB_STRONG,grade)[0] / 1000f).toString())
                .replace("-", OrbInfo.get(Data.ORB_STRONG,grade)[1].toString())
            Data.ORB_MASSIVE -> c.getString(R.string.orb_mas_desc)
                .replace("_", (OrbInfo.get(Data.ORB_MASSIVE,grade)[0] / 300f).toString())
            Data.ORB_RESISTANT -> c.getString(R.string.orb_res_desc)
                .replace("_", OrbInfo.get(Data.ORB_RESISTANT,grade)[0].toString())
            Data.ORB_MINIDEATHSURGE -> c.getString(R.string.orb_mds_desc)
                .replace("_", OrbInfo.get(Data.ORB_MINIDEATHSURGE,grade)[0].toString())
            Data.ORB_RESWAVE -> c.getString(R.string.orb_rsw_desc)
                .replace("_", OrbInfo.get(Data.ORB_RESWAVE,grade)[0].toString())
            Data.ORB_REFUND -> c.getString(R.string.orb_refn_desc)
                .replace("_", OrbInfo.get(Data.ORB_REFUND,grade)[0].toString())
            Data.ORB_RESKB -> c.getString(R.string.orb_rkb_desc)
                .replace("_", OrbInfo.get(Data.ORB_RESKB,grade)[0].toString())
            Data.ORB_SOLBUFF -> c.getString(R.string.orb_sol_desc)
                .replace("_", OrbInfo.get(Data.ORB_SOLBUFF,grade)[0].toString())
                .replace("-", OrbInfo.get(Data.ORB_SOLBUFF,grade)[1].toString())
            Data.ORB_BAKILL -> c.getString(R.string.orb_colo_desc)
                .replace("_", (OrbInfo.get(Data.ORB_BAKILL,grade)[0]-100).toString())
                .replace("-", (100-OrbInfo.get(Data.ORB_BAKILL,grade)[1]).toString())
            else -> "???"
        }

        text.visibility = View.VISIBLE
        text.text = s
    }

    private fun updateOrbData() {
        if(!this::f.isInitialized)
            return

        var str = false
        var mas = false
        var res = false

        val possibleTraits = ArrayList<Int>()

        val o = f.orbs ?: return

        val l = BasisSet.current().sele.lu.getLv(f)

        if(o.slots == -1) {
            for(form in f.unit.forms) {
                val mu = if(form.du.pCoin != null) {
                    form.du.pCoin.improve(l.talents)
                } else
                    form.du

                str = str or ((mu.proc.DMGINC.mult > 100 && mu.proc.DMGINC.mult < 300) || (mu.proc.DEFINC.mult > 100 && mu.proc.DEFINC.mult < 400))
                mas = mas or (mu.proc.DMGINC.mult >= 300)
                res = res or (mu.proc.DEFINC.mult >= 400)

                for(t in mu.traits) {
                    if(!t.BCTrait())
                        continue

                    val bitMask = 1 shl t.id.id
                    if(!possibleTraits.contains(bitMask))
                        possibleTraits.add(bitMask)
                }
            }
        } else {
            val mu = if(f.du.pCoin != null) {
                f.du.pCoin.improve(l.talents)
            } else
                f.du

            str = (mu.proc.DMGINC.mult > 100 && mu.proc.DMGINC.mult < 300) || (mu.proc.DEFINC.mult > 100 && mu.proc.DEFINC.mult < 400)
            mas = mu.proc.DMGINC.mult >= 300
            res = mu.proc.DEFINC.mult >= 400

            for(t in mu.traits) {
                if(!t.BCTrait())
                    continue

                val bitMask = 1 shl t.id.id

                if(!possibleTraits.contains(bitMask))
                    possibleTraits.add(bitMask)
            }
        }

        if(l.orbs != null) {
            for(data in l.orbs) {
                if(data.isEmpty())
                    continue

                if(!str && data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_STRONG)
                    data[Data.ORB_TYPE.toInt()] = Data.ORB_ATK.toInt()

                if(!mas && data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_MASSIVE)
                    data[Data.ORB_TYPE.toInt()] = Data.ORB_ATK.toInt()

                if(!res && data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_RESISTANT)
                    data[Data.ORB_TYPE.toInt()] = Data.ORB_ATK.toInt()

                if(needTraitFiltering(data)) {
                    val allTraits = CommonStatic.getBCAssets().ORB[data[0].toByte()]?.keys ?: continue

                    val traits = ArrayList<Int>()

                    for(t in possibleTraits) {
                        if(allTraits.contains(t))
                            traits.add(t)
                    }

                    if(traits.isNotEmpty())
                        traits.sortWith(Int::compareTo)

                    if(traits.isNotEmpty() && !traits.contains(data[Data.ORB_TRAIT.toInt()]))
                        data[Data.ORB_TRAIT.toInt()] = traits[0]
                }
            }
        }
    }

    private fun needTraitFiltering(data: IntArray) : Boolean {
        return data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_STRONG || data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_MASSIVE || data[Data.ORB_TYPE.toInt()].toByte() == Data.ORB_RESISTANT
    }
    
    private fun getTextIndex(id: Int) : Int {
        for(i in OrbInfo.orbTrait.indices)
            if(id == (1 shl OrbInfo.orbTrait[i].toInt()))
                return i
        
        return -1
    }
}