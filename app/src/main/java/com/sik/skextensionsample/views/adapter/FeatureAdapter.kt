package com.sik.skextensionsample.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sik.sikcore.anim.AnimConfig
import com.sik.sikcore.anim.PresetAnim
import com.sik.sikcore.anim.anim
import com.sik.skextensionsample.data.FeatureEntry
import com.sik.skextensionsample.R
import java.util.Stack

class FeatureAdapter() : RecyclerView.Adapter<FeatureAdapter.VH>() {
    private var data: Stack<List<FeatureEntry>> = Stack()
    private var back = FeatureEntry("返回上一级", "返回到上一级") {
        data.pop()
        notifyDataSetChanged()
    }

    class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.title)
        val desc = view.findViewById<TextView>(R.id.desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature_entry, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.post {
            holder.itemView.anim(PresetAnim.slideInFromRight())
        }
        val item = data.peek()[position]
        holder.title.text = item.title
        holder.desc.text = item.desc
        holder.itemView.setOnClickListener {
            if (item.children.isEmpty()) {
                item.action()
            } else {
                push(item.children)
            }
        }
    }

    override fun getItemCount() = data.peek().size

    /**
     * 下一级
     */
    private fun push(data: List<FeatureEntry>) {
        data.toMutableList().let {
            it.add(0, back)
            this.data.push(it)
        }
        notifyDataSetChanged()
    }

    /**
     * 设置数据
     */
    fun setData(data: List<FeatureEntry>) {
        this.data.clear()
        this.data.push(data)
        notifyDataSetChanged()
    }
}
