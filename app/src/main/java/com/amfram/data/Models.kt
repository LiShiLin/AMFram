package com.amfram.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class SourceType { Local, SMB, Unsplash }

enum class ShowMode { Slide, Fade, FrameWall, Bento, Calender }

@Parcelize
data class MediaItem(
    val name: String,
    val path: String,
    val isImage: Boolean = true
) : Parcelable

/** 数据源配置，密码等敏感字段加密存储。 */
@Parcelize
data class SourceConfig(
    val name: String,
    val type: SourceType,
    /** 本地路径 / Unsplash 集合名 / SMB 提示信息。 */
    val path: String = "",
    val host: String = "",
    val shareName: String = "",
    val username: String = "",
    /** 加密后的密码密文，内存保留密文，使用时解密。 */
    val encryptedPassword: String = ""
) : Parcelable {

    fun setPassword(plain: String): SourceConfig =
        copy(encryptedPassword = if (plain.isEmpty()) "" else com.amfram.util.RConfig.encrypt(plain))

    fun getPassword(): String =
        if (encryptedPassword.isEmpty()) "" else com.amfram.util.RConfig.decrypt(encryptedPassword)
}