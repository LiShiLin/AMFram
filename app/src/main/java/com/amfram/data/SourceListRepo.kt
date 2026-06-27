package com.amfram.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 数据源持久化（平台原生存储 SharedPreferences + JSON）。
 * 密码使用 RConfig 加密后存入。
 */
object SourceListRepo {
    private const val PREF_NAME = "amfram_sources"
    private const val KEY = "source_list"
    private val gson = Gson()

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun load(ctx: Context): List<SourceConfig> {
        val raw = prefs(ctx).getString(KEY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SourceConfig>>() {}.type
            gson.fromJson<List<SourceConfig>>(raw, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(ctx: Context, list: List<SourceConfig>) {
        prefs(ctx).edit().putString(KEY, gson.toJson(list)).apply()
    }

    fun add(ctx: Context, item: SourceConfig) {
        val list = load(ctx).toMutableList()
        val idx = list.indexOfFirst { it.name == item.name }
        if (idx >= 0) list[idx] = item else list.add(item)
        save(ctx, list)
    }

    fun remove(ctx: Context, name: String) {
        save(ctx, load(ctx).filter { it.name != name })
    }
}