package com.yumetsuki.bcu.androidutil.pack.adapters

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.yumetsuki.bcu.PackChapterManager
import com.yumetsuki.bcu.PackCreation
import com.yumetsuki.bcu.R
import com.yumetsuki.bcu.androidutil.StaticStore
import common.CommonStatic
import common.pack.PackData
import common.pack.UserProfile

class PackCreationAdapter(private val ac: PackCreation, private val pList: ArrayList<PackData.UserPack>) : ArrayAdapter<PackData.UserPack>(ac, R.layout.pack_create_list_layout, pList) {
    class ViewHolder(v: View) {
        val id = v.findViewById<TextView>(R.id.pcusid)!!
        val name = v.findViewById<EditText>(R.id.pcusname)!!
        val more = v.findViewById<FloatingActionButton>(R.id.pcusmore)!!
        val icn = v.findViewById<ImageView>(R.id.userpackIcon)!!
        val opts = v.findViewById<TableLayout>(R.id.pcusOptions)!!
        val chap = v.findViewById<Button>(R.id.pk_chapterEdit)!!
        val uni = v.findViewById<Button>(R.id.pk_unitEdit)!!

        val para = v.findViewById<RecyclerView>(R.id.pk_nonParentList)
        val par = v.findViewById<RecyclerView>(R.id.pk_parentList)
    }

    var dialog = AlertDialog.Builder(context)

    init {
        for(pack in UserProfile.getUserPacks())
            if (pack.editable)
                pList.add(pack)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        val row: View

        if (convertView == null) {
            val inf = LayoutInflater.from(context)

            row = inf.inflate(R.layout.pack_create_list_layout, parent, false)
            holder = ViewHolder(row)

            row.tag = holder
        } else {
            row = convertView
            holder = row.tag as ViewHolder
        }

        val p = pList[position]
        val title = if (p.desc.author == null || p.desc.author.isBlank()) {
            p.sid
        } else p.sid + " [${p.desc.author}]"

        holder.id.text = title
        holder.name.text = SpannableStringBuilder(p.desc.names.toString())
        holder.name.hint = p.sid
        if (p.icon == null)
            holder.icn.visibility = View.GONE
        else
            holder.icn.setImageBitmap(p.icon.img.bimg() as Bitmap)

        holder.name.setOnEditorActionListener { _, _, _ ->
            if (holder.name.text.toString() == p.desc.names.toString())
                return@setOnEditorActionListener false
            p.desc.names.put(holder.name.text.toString())
            false
        }

        val popup = PopupMenu(context, holder.more)
        val menu = popup.menu
        popup.menuInflater.inflate(R.menu.pack_list_option_menu, menu)

        popup.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.saveremove -> {
                    p.save.ulkUni.clear()
                    p.save.cSt.clear()
                    val sf = CommonStatic.ctx.getAuxFile("./saves/" + p.desc.id + ".packsave")
                    if (sf.exists())
                        sf.delete()
                    it.isVisible = false
                }
                R.id.packremove -> {
                    dialog.setTitle(R.string.pack_manage_remove_sure)
                    dialog.setMessage(R.string.pack_manage_remove_msg)

                    dialog.setPositiveButton(R.string.remove) { _, _ ->
                        UserProfile.unloadPack(p)
                        p.delete()

                        pList.remove(p)
                        notifyDataSetChanged()
                        StaticStore.showShortMessage(context, R.string.pack_remove_result)

                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                    dialog.setNegativeButton(R.string.main_file_cancel) {_, _ ->
                        ac.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                    }
                    StaticStore.fixOrientation(ac)

                    if (!ac.isDestroyed && !ac.isFinishing) {
                        dialog.show()
                    }
                }
                R.id.packshare -> {
                    //(p.source as Workspace).export(p, "", "", false, null);
                }
            }

            false
        }

        menu.getItem(1).isEnabled = !cantDelete(p)
        menu.getItem(2).isVisible = p.save?.cSt?.isNotEmpty() == true
        holder.more.setOnLongClickListener {
            popup.show()//Click will be to show the option buttons
            false
        }
        holder.more.setOnClickListener {
            holder.opts.visibility = View.GONE - holder.opts.visibility
        }
        holder.para.layoutManager = LinearLayoutManager(ac)
        val pada = PackParentAdapter(ac, p.desc, false)
        holder.para.adapter = pada
        holder.par.layoutManager = LinearLayoutManager(ac)
        val pad = PackParentAdapter(ac, p.desc, true)
        holder.par.adapter = pad

        ItemTouchHelper(object: ItemTouchHelper.Callback() {
            override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(0, ItemTouchHelper.END)
            }
            override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(h: RecyclerView.ViewHolder, j: Int) {
                val pos = h.bindingAdapterPosition
                val pacs = pada.getParentablePacks()
                val pac = pacs[pos]

                p.desc.dependency.add(pac.sid)
                pad.notifyItemInserted(p.desc.dependency.indexOf(pac.sid))
                pada.notifyItemRemoved(pos)
                for (dep in pac.desc.dependency) {
                    val pk = UserProfile.getUserPack(dep)
                    if (!pacs.contains(pk))
                        continue//TODO: Passwords
                    p.desc.dependency.add(pk.sid)
                    pad.notifyItemInserted(p.desc.dependency.indexOf(pk.sid))
                    val ind = pacs.indexOf(pk)
                    pacs.remove(ind)
                    pada.notifyItemRemoved(ind)
                }
            }
        }).attachToRecyclerView(holder.para)

        ItemTouchHelper(object: ItemTouchHelper.Callback() {
            override fun getMovementFlags(p0: RecyclerView, p1: RecyclerView.ViewHolder): Int {
                return makeMovementFlags(0, ItemTouchHelper.START)
            }
            override fun onMove(view: RecyclerView, src: RecyclerView.ViewHolder, dest: RecyclerView.ViewHolder): Boolean {
                return false
            }
            override fun onSwiped(h: RecyclerView.ViewHolder, j: Int) {
                val pos = h.bindingAdapterPosition
                val pac = p.desc.dependency.get(pos)
                p.desc.dependency.remove(pos)
                pad.notifyItemRemoved(pos)

                val packs = pada.getParentablePacks()
                for (i in packs.indices)
                    if (packs[i].sid == pac) {
                        pada.notifyItemInserted(i)
                        break
                    }
            }
        }).attachToRecyclerView(holder.par)

        holder.chap.setOnClickListener {
            val intent = Intent(context, PackChapterManager::class.java)
            intent.putExtra("pack", p.sid)

            ac.startActivity(intent)
        }
        //holder.uni.setOnClickListener {
            //val intent = Intent(context, PackEntityManager::class.java)
            //intent.putExtra("pack", p.sid)

            //ac.startActivity(intent)
        //}
        return row
    }

    private fun cantDelete(p: PackData.UserPack) : Boolean {
        for(pack in UserProfile.getAllPacks()) {
            pack ?: continue

            if(pack is PackData.DefPack || pack.sid == p.sid)
                continue

            if(pack is PackData.UserPack) {
                for(pid in pack.desc.dependency) {
                    if(pid == p.sid)
                        return true
                }
            }
        }
        return false
    }
}