
package com.qrissoundbox

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import android.os.Bundle
import java.util.Locale

class NotificationReaderService : NotificationListenerService(),
    TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingMessage: String? = null

    private var lastMessage = ""
    private var lastSpokenTime = 0L

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("id", "ID"))

            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED

            if (ttsReady) {
                pendingMessage?.let {
                    speak(it)
                    pendingMessage = null
                }
            }
        }
    }

    override fun onNotificationPosted(
        sbn: StatusBarNotification
    ) {
        val extras: Bundle = sbn.notification.extras

        val title = extras.getCharSequence(
            "android.title"
        )?.toString() ?: ""

        val text = extras.getCharSequence(
            "android.text"
        )?.toString() ?: ""

        val bigText = extras.getCharSequence(
            "android.bigText"
        )?.toString() ?: ""

        val message = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(". ")

        // Tahap pengujian: cari notifikasi yang mengandung nominal.
        val hasAmount = Regex(
            """(?i)(Rp\.?\s*\d|IDR\s*\d)"""
        ).containsMatchIn(message)

        if (!hasAmount) return

        val now = System.currentTimeMillis()

        // Cegah pesan identik dibacakan berulang dalam 5 detik.
        if (message == lastMessage &&
            now - lastSpokenTime < 5000
        ) return

        lastMessage = message
        lastSpokenTime = now

        if (ttsReady) {
            speak(message)
        } else {
            pendingMessage = message
        }
    }

    private fun speak(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
            null,
            "qris-${System.currentTimeMillis()}"
        )
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}

