
package com.qrissoundbox

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "QRIS SOUNDBOX"
            textSize = 28f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 32)
        }

        val button = Button(this).apply {
            text = "AKTIFKAN AKSES NOTIFIKASI"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                )
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(button)

        setContentView(layout)
        updateStatus()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val enabled = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )

        val component = "$packageName/${NotificationReaderService::class.java.name}"

        val isEnabled = !TextUtils.isEmpty(enabled) &&
                enabled.contains(component)

        statusText.text = if (isEnabled) {
            "Akses notifikasi: AKTIF"
        } else {
            "Akses notifikasi: BELUM AKTIF"
        }
    }
}
