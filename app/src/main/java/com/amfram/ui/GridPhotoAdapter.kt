package com.amfram.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.transform.BlurTransformation
import com.amfram.data.MediaItem

/**
 * 宫格图片适配器
 *  - mode = "center": 模糊背景铺满 + 前景居中显示
 *  - mode = "fill":   前景 centerCrop 直接铺满
 *  - replaceItem(position, item): 渐隐前景 → 加载新图 → 渐显前景（同步刷新背景）
 */
class GridPhotoAdapter(
    private val imageLoader: ImageLoader,
    private val itemWpx: Int,
    private val itemHpx: Int,
    private val mode: String // "center" / "fill"
) : RecyclerView.Adapter<GridPhotoAdapter.VH>() {

    class VH(val binding: com.amfram.databinding.ItemPhotoGridBinding) : RecyclerView.ViewHolder(binding.root)

    private val items = mutableListOf<MediaItem>()

    fun submitList(list: List<MediaItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun itemAt(pos: Int): MediaItem? = items.getOrNull(pos)

    /** 单格替换，含渐隐渐显动画 */
    fun replaceItem(position: Int, newItem: MediaItem) {
        if (position !in items.indices) return
        items[position] = newItem
        notifyItemChanged(position)
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
        val ctx = holder.itemView.context

        if (mode == "center") {
            holder.binding.bgView.visibility = android.view.View.VISIBLE
            holder.binding.photoView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            val bgData = toLoadData(item.path)
            val bgReq = ImageRequest.Builder(ctx)
                .data(bgData)
                .transformations(BlurTransformation(ctx, 25f))
                .crossfade(true)
                .target(holder.binding.bgView)
                .build()
            imageLoader.enqueue(bgReq)
        } else {
            holder.binding.bgView.visibility = android.view.View.GONE
            holder.binding.photoView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }

        // 前景图：从透明淡入显示（替换动画的"渐显"部分）
        holder.binding.photoView.alpha = 0f
        val req = ImageRequest.Builder(ctx)
            .data(toLoadData(item.path))
            .crossfade(false)
            .target(holder.binding.photoView)
            .listener(object : ImageRequest.Listener {
                override fun onSuccess(request: ImageRequest, metadata: ImageResult.Metadata) {
                    val anim = AlphaAnimation(0f, 1f).apply { duration = 500 }
                    holder.binding.photoView.startAnimation(anim)
                    holder.binding.photoView.alpha = 1f
                }
                override fun onError(request: ImageRequest, throwable: Throwable) {
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