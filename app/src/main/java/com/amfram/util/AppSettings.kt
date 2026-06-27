package com.amfram.util

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatDelegate
import com.amfram.data.ShowMode

/**
 * 应用设置（明暗主题、默认展示模式、播放间隔），基于 SharedPreferences。
 * 适配 Android 4.4：使用 AppCompatDelegate 设夜模式（API 19 实际只切回 default 主题 fallback）。
 */
object AppSettings {
    private const val PREF = "amfram_settings"
    private const val KEY_DARK = "dark_mode"
    private const val KEY_MODE = "show_mode"
    private const val KEY_INTERVAL = "interval_seconds"
    private const val KEY_GRID_ROWS = "grid_rows"
    private const val KEY_GRID_COLS = "grid_cols"
    private const val KEY_GRID_SPACING = "grid_spacing_dp" // 1..5
    private const val KEY_GRID_MODE = "grid_mode" // center / fill
    private const val KEY_ORIENTATION = "orientation" // 0=auto, 1=portrait, 2=landscape
    private const val KEY_REPLACE_INTERVAL = "replace_interval_seconds"

    fun prefs(ctx: Context) = ctx.applicationContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun isDark(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_DARK, false)

    fun setDark(ctx: Context, dark: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_DARK, dark).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun applyTheme(ctx: Context) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark(ctx)) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    fun showModeOrdinal(ctx: Context): Int =
        prefs(ctx).getInt(KEY_MODE, 0).coerceIn(0, ShowMode.values().lastIndex)

    fun setShowMode(ctx: Context, ordinal: Int) =
        prefs(ctx).edit().putInt(KEY_MODE, ordinal).apply()

    fun intervalSeconds(ctx: Context): Int =
        prefs(ctx).getInt(KEY_INTERVAL, 5)

    fun setInterval(ctx: Context, sec: Int) =
        prefs(ctx).edit().putInt(KEY_INTERVAL, sec).apply()

    fun gridRows(ctx: Context): Int = prefs(ctx).getInt(KEY_GRID_ROWS, 3)

    fun setGridRows(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt(KEY_GRID_ROWS, v).apply()

    fun gridCols(ctx: Context): Int = prefs(ctx).getInt(KEY_GRID_COLS, 3)

    fun setGridCols(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt(KEY_GRID_COLS, v).apply()

    fun gridSpacingDp(ctx: Context): Int = prefs(ctx).getInt(KEY_GRID_SPACING, 2).coerceIn(1, 5)

    fun setGridSpacing(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt(KEY_GRID_SPACING, v.coerceIn(1, 5)).apply()

    fun gridMode(ctx: Context): String = prefs(ctx).getString(KEY_GRID_MODE, "center") ?: "center"

    fun setGridMode(ctx: Context, mode: String) =
        prefs(ctx).edit().putString(KEY_GRID_MODE, mode).apply()

    fun orientation(ctx: Context): Int = prefs(ctx).getInt(KEY_ORIENTATION, 0)

    fun setOrientation(ctx: Context, v: Int) =
        prefs(ctx).edit().putInt(KEY_ORIENTATION, v).apply()

    fun applyOrientation(activity: androidx.appcompat.app.AppCompatActivity) {
        when (orientation(activity)) {
            1 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            2 -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            else -> activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    fun replaceIntervalSeconds(ctx: Context): Int = prefs(ctx).getInt(KEY_REPLACE_INTERVAL, 5)

    fun setReplaceInterval(ctx: Context, sec: Int) =
        prefs(ctx).edit().putInt(KEY_REPLACE_INTERVAL, sec).apply()
}