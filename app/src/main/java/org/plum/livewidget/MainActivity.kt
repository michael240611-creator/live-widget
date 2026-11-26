package org.plum.livewidget

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
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

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(50, 100, 50, 50)
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "Video Live Wallpaper"
            textSize = 28f
            setTextColor(Color.parseColor("#FFCA28"))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val btnSelect = Button(this).apply {
            text = "Выбрать Видео"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener { pickVideo.launch("video/*") }
        }

        val btnSetWallpaper = Button(this).apply {
            text = "Установить как обои"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(this@MainActivity, VideoLiveWallpaperService::class.java))
                }
                startActivity(intent)
            }
        }

        layout.addView(title)
        layout.addView(btnSelect)
        layout.addView(btnSetWallpaper)
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
            Toast.makeText(this, "Видео сохранено! Нажмите 'Установить как обои'", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
