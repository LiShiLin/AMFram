package com.amfram.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager.widget.ViewPager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.amfram.data.MediaItem
import com.amfram.data.RepoManager
import com.amfram.data.ShowMode
import com.amfram.data.SourceConfig
import com.amfram.databinding.ActivityPlayBinding
import com.amfram.util.AppSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding
    private lateinit var imageLoader: ImageLoader
    private lateinit var pagerAdapter: PhotoAdapter
    private var gridAdapter: GridPhotoAdapter? = null
    private var showMode: ShowMode = ShowMode.Slide
    private var items: List<MediaItem> = emptyList()
    private var playing = false
    private var controlsVisible = false
    private val hideHandler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable { hideControls() }

    private val gestureDetector by lazy {
        android.view.GestureDetector(this, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: android.view.MotionEvent): Boolean {
                toggleControls()
                return true
            }
        })
    }

    override fun dispatchTouchEvent(ev: android.view.MotionEvent?): Boolean {
        ev?.let { gestureDetector.onTouchEvent(it) }
        return super.dispatchTouchEvent(ev)
    }

    // 宫格替换状态
    private val usedPositions = mutableSetOf<Int>()
    private var nextImagePointer = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSettings.applyTheme(this)
        AppSettings.applyOrientation(this)
        super.onCreate(savedInstanceState)
        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enterImmersive()

        binding.backButton.setOnClickListener { finish() }

        imageLoader = (applicationContext as? ImageLoaderFactory)?.newImageLoader()
            ?: ImageLoader.Builder(this).build()
        pagerAdapter = PhotoAdapter(imageLoader)
        binding.pager.adapter = pagerAdapter

        showMode = ShowMode.values()[AppSettings.showModeOrdinal(this)]

        val source = intent.getParcelableExtra<SourceConfig>(EXTRA_SOURCE)
        if (source == null) { finish(); return }

        setupViewByMode()
        load(source)
    }

    private fun setupViewByMode() {
        if (showMode == ShowMode.FrameWall) {
            binding.pager.visibility = View.GONE
            binding.gridView.visibility = View.VISIBLE
            val cols = AppSettings.gridCols(this).coerceIn(1, 20)
            val rows = AppSettings.gridRows(this).coerceIn(1, 20)
            val dm = resources.displayMetrics
            // 相框边距 (1..5 dp → px)
            val spacingDp = AppSettings.gridSpacingDp(this)
            val spacingPx = (spacingDp * (dm.densityDpi / 160f)).toInt()
            // 每格宽独立按列均分，高独立按行均分，铺满整屏
            val cellW = (dm.widthPixels - (cols + 1) * spacingPx) / cols
            val cellH = (dm.heightPixels - (rows + 1) * spacingPx) / rows
            gridAdapter = GridPhotoAdapter(
                imageLoader,
                cellW, cellH,
                AppSettings.gridMode(this)
            ).also { it.recyclerView = binding.gridView }
            with(binding.gridView) {
                layoutManager = GridLayoutManager(this@PlayActivity, cols)
                adapter = gridAdapter
                setPadding(spacingPx, spacingPx, spacingPx, spacingPx)
                addItemDecoration(object : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: android.graphics.Rect,
                        view: android.view.View,
                        parent: androidx.recyclerview.widget.RecyclerView,
                        state: androidx.recyclerview.widget.RecyclerView.State
                    ) {
                        outRect.set(spacingPx, spacingPx, spacingPx, spacingPx)
                    }
                })
                isNestedScrollingEnabled = false
                overScrollMode = android.view.View.OVER_SCROLL_NEVER
                clipToPadding = false
            }
        } else {
            binding.pager.visibility = View.VISIBLE
            binding.gridView.visibility = View.GONE
        }
    }

    private fun load(source: SourceConfig) {
        binding.loading.visibility = View.VISIBLE
        lifecycleScope.launch {
            items = RepoManager.listImages(this@PlayActivity, source)
            if (items.isEmpty()) {
                binding.loading.visibility = View.GONE
                return@launch
            }
            if (showMode == ShowMode.FrameWall) {
                val cols = AppSettings.gridCols(this@PlayActivity).coerceIn(1, 20)
                val rows = AppSettings.gridRows(this@PlayActivity).coerceIn(1, 20)
                val cellCount = rows * cols
                // 初始只放 rows*cols 个格子，固定不可滑动
                val isRandom = AppSettings.replaceOrder(this@PlayActivity) == "random"
                gridAdapter?.submitList(
                    if (isRandom) items.shuffled().take(cellCount) else items.take(cellCount)
                )
                startGridReplaceLoop()
            } else {
                pagerAdapter.submitList(items)
                startSlideAutoPlay()
            }
            binding.loading.visibility = View.GONE
        }
    }

    /** Slide/Fade/Bento/Calender 的自动翻页 */
    private fun startSlideAutoPlay() {
        if (playing) return
        playing = true
        val interval = AppSettings.intervalSeconds(this).toLong()
        lifecycleScope.launch {
            while (playing && items.isNotEmpty()) {
                delay(interval * 1000)
                val next = (binding.pager.currentItem + 1) % items.size
                binding.pager.setCurrentItem(next, true)
            }
        }
    }

    /**
     * 宫格模式：每 [replaceInterval] 秒替换一格（渐隐渐显）；
     * 随机模式：从图库随机挑一张图，随机选格子替换；
     * 顺序模式：按图库列表顺序逐格替换。
     */
    private fun startGridReplaceLoop() {
        if (playing) return
        playing = true
        val interval = AppSettings.replaceIntervalSeconds(this).toLong().coerceAtLeast(1)
        val total = items.size
        if (total == 0) return
        val cols = AppSettings.gridCols(this).coerceIn(1, 20)
        val rows = AppSettings.gridRows(this).coerceIn(1, 20)
        val cellCount = rows * cols
        val isRandom = AppSettings.replaceOrder(this) == "random"
        // 顺序模式从初始已展示的图片之后开始取新图
        nextImagePointer = cellCount
        lifecycleScope.launch {
            delay(interval * 1000)
            while (playing && gridAdapter != null) {
                val adapter = gridAdapter ?: break
                if (cellCount == 0) break

                // 选格子
                val pos: Int
                if (isRandom) {
                    if (usedPositions.size >= cellCount) usedPositions.clear()
                    val candidates = (0 until cellCount).filter { it !in usedPositions }
                    pos = if (candidates.isNotEmpty()) candidates[Random.nextInt(candidates.size)]
                        else Random.nextInt(cellCount)
                    usedPositions.add(pos)
                } else {
                    pos = (nextImagePointer - cellCount) % cellCount
                }

                // 选图片
                val newItem = if (isRandom) {
                    items[Random.nextInt(total)]
                } else {
                    items[nextImagePointer % total]
                }
                nextImagePointer++

                runOnUiThread { adapter.replaceItem(pos, newItem) }

                delay(interval * 1000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        playing = false
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersive()
    }

    private fun enterImmersive() {
        // 隐藏状态栏与导航栏，全屏沉浸
        val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        window.decorView.systemUiVisibility = flags
        // 兼容 4.4 也用 ActionBar 隐藏走 requestWindowFeature
        supportActionBar?.hide()
    }

    private fun toggleControls() {
        if (controlsVisible) hideControls() else showControls()
    }

    private fun showControls() {
        controlsVisible = true
        binding.backButton.visibility = View.VISIBLE
        binding.backButton.animate().alpha(1f).setDuration(200).start()
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 3000)
    }

    private fun hideControls() {
        controlsVisible = false
        binding.backButton.animate().alpha(0f).setDuration(200).withEndAction {
            binding.backButton.visibility = View.GONE
        }.start()
    }

    companion object {
        const val EXTRA_SOURCE = "extra_source"
    }
}