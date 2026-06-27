package com.amfram.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amfram.data.SourceConfig
import com.amfram.databinding.ItemSourceBinding

class SourceAdapter(
    private val onPlay: (SourceConfig) -> Unit,
    private val onLongClick: (SourceConfig) -> Unit
) : ListAdapter<SourceConfig, SourceAdapter.VH>(DIFF) {

    class VH(val binding: ItemSourceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.sourceName.text = item.name
        holder.binding.sourceType.text = item.type.name
        holder.binding.btnPlay.setOnClickListener { onPlay(item) }
        holder.itemView.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<SourceConfig>() {
            override fun areItemsTheSame(o: SourceConfig, n: SourceConfig) = o.name == n.name
            override fun areContentsTheSame(o: SourceConfig, n: SourceConfig) = o == n
        }
    }
}