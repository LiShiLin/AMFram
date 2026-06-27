package com.amfram.data

import com.hierynomus.msfscc.FileAttributes
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * SMB 客户端管理器，移植自原 MyRestart 项目。
 * 兼容 Android 4.4（纯 Java IO 实现，无新 API 依赖）。
 */
class SMBClientManager(
    private val host: String,
    private val username: String,
    private val password: String,
    private val shareName: String
) {
    private var client: SMBClient? = null
    private var share: DiskShare? = null

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            client = SMBClient()
            val connection = client!!.connect(host)
            val authContext = AuthenticationContext(username, password.toCharArray(), null)
            val session = connection.authenticate(authContext)
            share = session.connectShare(shareName) as? DiskShare
            share != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun listFiles(path: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<MediaItem>()
        try {
            share?.let { s ->
                for (file in s.list(path)) {
                    val isDir = file.fileAttributes and FileAttributes.FILE_ATTRIBUTE_DIRECTORY.value != 0L
                    val name = file.fileName
                    if (name == "." || name == "..") continue
                    result.add(
                        MediaItem(
                            name = name,
                            path = if (path == "/") "/$name" else "$path/$name",
                            isImage = !isDir && isImageFile(name)
                        )
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        result
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
            lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".bmp")
    }

    fun disconnect() {
        try { share?.close() } catch (_: Exception) {}
        try { client?.close() } catch (_: Exception) {}
    }
}