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
    // ok: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance("SHA-256")
    // ok: fasttimes.kotlin.security.weak-message-digest
    MessageDigest.getInstance(algorithm)
}

private fun insecureCipher() {
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES")
    // ruleid: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/ECB/PKCS5Padding")
    // ok: fasttimes.kotlin.security.ecb-cipher
    Cipher.getInstance("AES/GCM/NoPadding")
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

private fun pendingIntentMutability(context: Context, intent: Intent) {
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getActivity(context, 0, intent, 0)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getBroadcast(context, 0, intent, 0)
    // ruleid: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getService(context, 0, intent, 0)
    // ok: fasttimes.android.security.pending-intent-without-mutability
    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
}
