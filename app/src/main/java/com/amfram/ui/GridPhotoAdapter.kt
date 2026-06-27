package com.amfram.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.transform.BlurTransformation
import com.amfram.data.MediaItem

class GridPhotoAdapter(
    private val imageLoader: ImageLoader,
    private val itemWpx: Int,
    private val itemHpx: Int,
    private val mode: String
) : RecyclerView.Adapter<GridPhotoAdapter.VH>() {

    var recyclerView: RecyclerView? = null

    class VH(val binding: com.amfram.databinding.ItemPhotoGridBinding) : RecyclerView.ViewHolder(binding.root)

    private val items = mutableListOf<MediaItem>()

    fun submitList(list: List<MediaItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun itemAt(pos: Int): MediaItem? = items.getOrNull(pos)

    fun replaceItem(position: Int, newItem: MediaItem) {
        if (position !in items.indices) return
        items[position] = newItem
        val holder = recyclerView?.findViewHolderForAdapterPosition(position) as? VH
        if (holder != null) {
            crossfade(holder, newItem)
        } else {
            notifyItemChanged(position)
        }
    }

    private fun crossfade(holder: VH, newItem: MediaItem) {
        val oldFg = holder.binding.photoView
        val oldBg = holder.binding.bgView

        oldFg.animate().alpha(0f).setDuration(400).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                oldFg.animate().setListener(null)
                bindContent(holder, newItem)
            }
        }).start()
        if (mode == "center" && oldBg.visibility == android.view.View.VISIBLE) {
            oldBg.animate().alpha(0f).setDuration(400).start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = com.amfram.databinding.ItemPhotoGridBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        b.root.layoutParams = ViewGroup.LayoutParams(itemWpx, itemHpx)
        return VH(b)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        bindContent(holder, item)
    }

    private fun bindContent(holder: VH, item: MediaItem) {
        val ctx = holder.itemView.context

        holder.binding.photoView.alpha = 0f
        holder.binding.photoView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

        if (mode == "center") {
            holder.binding.bgView.visibility = android.view.View.VISIBLE
            holder.binding.bgView.alpha = 0f
            holder.binding.photoView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER

            val bgReq = ImageRequest.Builder(ctx)
                .data(toLoadData(item.path))
                .transformations(BlurTransformation(ctx, 25f))
                .crossfade(false)
                .target(holder.binding.bgView)
                .build()
            imageLoader.enqueue(bgReq)
        } else {
            holder.binding.bgView.visibility = android.view.View.GONE
        }

        val req = ImageRequest.Builder(ctx)
            .data(toLoadData(item.path))
            .crossfade(false)
            .target(holder.binding.photoView)
            .listener(object : ImageRequest.Listener {
                override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                    holder.binding.photoView.animate().alpha(1f).setDuration(1000).start()
                    if (mode == "center") {
                        holder.binding.bgView.animate().alpha(1f).setDuration(1000).start()
                    }
                }
                override fun onError(request: ImageRequest, throwable: Throwable) {
                    holder.binding.photoView.alpha = 1f
                    if (mode == "center") {
                        holder.binding.bgView.alpha = 1f
                    }
                    android.util.Log.e("AMFram", "Grid photo load FAIL: ${item.path}", throwable)
                }
            })
            .build()
        imageLoader.enqueue(req)
    }

    companion object {
        private fun toLoadData(path: String): Any {
            return when {
                path.startsWith("http") || path.startsWith("https") -> path
                path.startsWith("content://") -> android.net.Uri.parse(path)
                path.startsWith("file://") -> java.io.File(android.net.Uri.parse(path).path ?: path)
                else -> {
                    val file = java.io.File(path)
                    if (file.exists()) file else android.net.Uri.fromFile(file)
                }
            }
        }
    }
}
