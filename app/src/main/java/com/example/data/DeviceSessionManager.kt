package com.example.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Local persistent device identity & cryptographic post-ownership proof manager.
 * Keeps a stable per-device anonymous ID and SHA-256 delete proof so users can
 * publish in real time and securely delete their own posts without any login wall.
 */
class DeviceSessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("yangmei_device_session_v1", Context.MODE_PRIVATE)
    private val legacyPrefs = context.getSharedPreferences("yangmei_wechat_account_v1", Context.MODE_PRIVATE)

    val currentPublicUid: String
    val currentVaultId: String

    init {
        val existingUid = prefs.getString("public_uid", "").orEmpty()
            .ifBlank { legacyPrefs.getString("public_uid", "").orEmpty() }
        val existingVault = prefs.getString("vault_id", "").orEmpty()
            .ifBlank { legacyPrefs.getString("vault_id", "").orEmpty() }

        if (existingUid.isNotBlank() && existingVault.isNotBlank()) {
            currentPublicUid = existingUid
            currentVaultId = existingVault
        } else {
            currentPublicUid = "ym_${randomHex(20)}"
            currentVaultId = "ymv_${randomHex(32)}"
        }

        prefs.edit()
            .putString("public_uid", currentPublicUid)
            .putString("vault_id", currentVaultId)
            .apply()
    }

    fun computeDeleteProof(postId: String): String {
        return sha256Hex("ym_dp_v1:$postId:$currentVaultId")
    }

    fun computeDeleteVerifier(postId: String): String {
        return sha256Hex(computeDeleteProof(postId))
    }

    companion object {
        fun sha256Hex(input: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            val sb = StringBuilder(bytes.size * 2)
            for (b in bytes) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString()
        }

        private fun randomHex(len: Int): String {
            val rng = SecureRandom()
            val bytes = ByteArray((len + 1) / 2)
            rng.nextBytes(bytes)
            val sb = StringBuilder(bytes.size * 2)
            for (b in bytes) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString().take(len)
        }
    }
}
