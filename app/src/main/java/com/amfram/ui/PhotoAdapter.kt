package com.amfram.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import coil.ImageLoader
import coil.request.ImageRequest
import com.amfram.data.MediaItem

/**
 * 用传统 ViewPager 1.x 实现的图片幻灯片适配器，兼容 Android 4.4 (API 19)。
 */
class PhotoAdapter(private val imageLoader: ImageLoader) : PagerAdapter() {

    private val items = mutableListOf<MediaItem>()

    fun submitList(list: List<MediaItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view === `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val ctx = container.context
        val photoView = ImageView(ctx).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        val item = items[position]
        val req = ImageRequest.Builder(ctx)
            .data(toLoadData(item.path))
            .crossfade(true)
            .target(photoView)
            .build()
        imageLoader.enqueue(req)
        container.addView(photoView)
        return photoView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

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