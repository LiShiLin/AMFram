package com.amfram.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * RepoManager：数据源核心管理器。
 * Local (SAF tree URI) / SMB / Unsplash 三种来源统一列出图片项。
 */
object RepoManager {

    suspend fun listImages(ctx: Context, source: SourceConfig): List<MediaItem> =
        withContext(Dispatchers.IO) {
            when (source.type) {
                SourceType.Local -> listLocal(ctx, source)
                SourceType.SMB -> listSmb(source)
                SourceType.Unsplash -> unsplashSamples()
            }
        }

    private fun listLocal(ctx: Context, source: SourceConfig): List<MediaItem> {
        android.util.Log.d("AMFram", "listLocal path=${source.path}")
        if (source.path.isEmpty()) {
            android.util.Log.d("AMFram", "empty path → unsplash")
            return unsplashSamples()
        }
        val raw = if (source.path.startsWith("file://")) source.path.removePrefix("file://") else source.path
        val dir = java.io.File(raw)
        android.util.Log.d("AMFram", "dir=${dir.absolutePath} exists=${dir.exists()} isDir=${dir.isDirectory} canRead=${dir.canRead()}")
        if (dir.exists() && dir.isDirectory) {
            val out = mutableListOf<MediaItem>()
            walkFile(dir, out)
            android.util.Log.d("AMFram", "walked ${out.size} images")
            return out.ifEmpty { unsplashSamples() }
        }
        val root = runCatching { DocumentFile.fromTreeUri(ctx, Uri.parse(source.path)) }.getOrNull()
            ?: return emptyList()
        if (!root.isDirectory) return emptyList()
        val result = mutableListOf<MediaItem>()
        walk(ctx, root, result)
        return result.ifEmpty { unsplashSamples() }
    }

    private fun walkFile(dir: java.io.File, out: MutableList<MediaItem>) {
        val children = dir.listFiles() ?: return
        for (f in children) {
            if (f.isDirectory) walkFile(f, out)
            else if (f.isFile && isImageFile(f.name)) out.add(MediaItem(f.name, android.net.Uri.fromFile(f).toString(), true))
        }
    }

    private fun walk(ctx: Context, dir: DocumentFile, out: MutableList<MediaItem>) {
        for (doc in dir.listFiles()) {
            if (doc.isDirectory) {
                walk(ctx, doc, out)
            } else if (doc.isFile && isImageFile(doc.name ?: "")) {
                out.add(MediaItem(doc.name ?: "", doc.uri.toString(), true))
            }
        }
    }

    private suspend fun listSmb(source: SourceConfig): List<MediaItem> {
        val smb = SMBClientManager(
            host = source.host,
            username = source.username,
            password = source.getPassword(),
            shareName = source.shareName
        )
        val ok = smb.connect()
        if (!ok) return emptyList()
        return smb.listFiles("/")
    }

    private fun unsplashSamples(): List<MediaItem> {
        return listOf(
            "photo-1506744038136-46283814a23c",
            "photo-1518791841217-8f162f1e1131",
            "photo-1543342384-89b5a3f7e21b",
            "photo-1507004510999-7977b4b7d96c",
            "photo-1503023345780-2f79f6d24f23",
            "photo-1438761681033-6461ffad8d80"
        ).mapIndexed { i, id ->
            MediaItem("sample_$i", "https://images.unsplash.com/$id?w=1080", true)
        }
    }

    private fun isImageFile(name: String): Boolean {
        val l = name.lowercase()
        return l.endsWith(".jpg") || l.endsWith(".jpeg") || l.endsWith(".png") ||
            l.endsWith(".webp") || l.endsWith(".gif") || l.endsWith(".bmp")
    }
}