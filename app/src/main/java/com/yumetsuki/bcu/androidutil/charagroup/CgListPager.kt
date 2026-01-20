package com.yumetsuki.bcu.androidutil.charagroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.yumetsuki.bcu.R
import common.pack.Identifier
import common.pack.PackData
import common.pack.UserProfile

class CgListPager : Fragment() {

    companion object {
        fun newInstance(pid: String) : CgListPager {
            val cs = CgListPager()
            val bundle = Bundle()

            bundle.putString("pid", pid)
            cs.arguments = bundle

            return cs
        }
    }
    private var pid = Identifier.DEF

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val c = context ?: return null
        val view = inflater.inflate(R.layout.entity_list_pager, container, false)

        pid = arguments?.getString("pid") ?: Identifier.DEF

        val list = view.findViewById<ListView>(R.id.entitylist)
        val nores = view.findViewById<TextView>(R.id.entitynores)

        val p = UserProfile.getPack(pid)
        var index = -1

        return view
    }
}