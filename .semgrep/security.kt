package semgrep.tests

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.webkit.WebSettings
import java.security.MessageDigest
import javax.crypto.Cipher

@Suppress("UNUSED_PARAMETER")
private fun weakDigests(algorithm: String) {
    // ruleid: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("MD5")
    // ruleid: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("md5")
    // ruleid: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("SHA1")
    // ruleid: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("MD5", "BC")
    // ruleid: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance(" MD5 ")
    // ok: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("SHA-256")
    // ok: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("SHA-256", "BC")
    // ok: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance(algorithm)
}

private fun insecureCipher() {
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES")
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/ECB/PKCS5Padding")
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("aes/ecb/pkcs5padding")
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/ECB/PKCS5Padding", "BC")
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance(" AES / ECB / PKCS5Padding ")
    // ok: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/GCM/NoPadding")
    // ok: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/GCM/NoPadding", "BC")
}

@Suppress("DEPRECATION")
private fun insecureWebViewSettings(settings: WebSettings) {
    // ruleid: fasttimes.android.security.webview-file-url-access
    settings.allowFileAccessFromFileURLs = true
    // ruleid: fasttimes.android.security.webview-file-url-access
    settings.allowUniversalAccessFromFileURLs = true
    // ok: fasttimes.android.security.webview-file-url-access
    settings.allowFileAccessFromFileURLs = false
    // ok: fasttimes.android.security.webview-file-url-access
    settings.allowUniversalAccessFromFileURLs = false
}

@Suppress("UNUSED_PARAMETER")
private fun pendingIntentMutability(context: Context, intent: Intent, mutable: Boolean) {
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getActivity(context, 0, intent, 0)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getBroadcast(context, 0, intent, 0)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getService(context, 0, intent, 0)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getBroadcast(
        context,
        0,
        intent,
        if (mutable) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
    )
    // ok: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    // ok: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    // ok: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_MUTABLE)
}
