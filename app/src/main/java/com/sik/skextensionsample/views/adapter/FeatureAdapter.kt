package com.sik.skextensionsample.views.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sik.skextensionsample.data.FeatureEntry
import com.sik.skextensionsample.R

class FeatureAdapter(private val data: List<FeatureEntry>) :
    RecyclerView.Adapter<FeatureAdapter.VH>() {

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
        val item = data[position]
        holder.title.text = item.title
        holder.desc.text = item.desc
        holder.itemView.setOnClickListener { item.action() }
    }

    override fun getItemCount() = data.size
}
