package com.amfram.util

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 简单 AES-128-CBC 加密工具，用于加密敏感配置（密码/Token）。
 * 兼容 Android 4.4 (API 19)，仅使用 javax.crypto。
 */
object RConfig {

    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY = "AMFramStaticK3y!" // 16 bytes，演示用，生产应做密钥派生

    private val keySpec = SecretKeySpec(KEY.toByteArray(Charsets.UTF_8), ALGORITHM)

    fun encrypt(plain: String): String {
        return try {
            val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, IvParameterSpec(iv))
            val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            val combined = iv + encrypted
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plain
        }
    }

    fun decrypt(cipherText: String): String {
        return try {
            val combined = Base64.decode(cipherText, Base64.NO_WRAP)
            val iv = combined.copyOfRange(0, 16)
            val encrypted = combined.copyOfRange(16, combined.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
            String(cipher.doFinal(encrypted), Charsets.UTF_8)
        } catch (e: Exception) {
            cipherText
        }
    }
}