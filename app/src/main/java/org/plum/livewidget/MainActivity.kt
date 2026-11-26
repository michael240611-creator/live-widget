package org.plum.livewidget

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private val pickVideo = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            saveVideoToInternal(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка разрешения на оверлей
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
            Toast.makeText(this, "Разрешите показ поверх других приложений", Toast.LENGTH_LONG).show()
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }

        val btnSelect = Button(this).apply {
            text = "1. Выбрать Видео"
            setOnClickListener { pickVideo.launch("video/*") }
        }

        val btnStartOverlay = Button(this).apply {
            text = "2. Запустить видео на экране"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "Сначала разрешите оверлей!", Toast.LENGTH_SHORT).show()
                } else {
                    startService(Intent(this@MainActivity, VideoOverlayService::class.java))
                    Toast.makeText(this@MainActivity, "Видео запущено! Сверните приложение.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        layout.addView(btnSelect)
        layout.addView(btnStartOverlay)
        setContentView(layout)
    }

    private fun saveVideoToInternal(uri: Uri) {
        try {
            val file = File(filesDir, "live_wallpaper_video.mp4")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
            prefs.edit().putString("video_path", file.absolutePath).apply()
            Toast.makeText(this, "Видео сохранено!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
