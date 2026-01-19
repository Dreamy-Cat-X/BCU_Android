package com.yumetsuki.bcu.androidutil.charagroup

import android.os.Bundle
import androidx.fragment.app.Fragment

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
}