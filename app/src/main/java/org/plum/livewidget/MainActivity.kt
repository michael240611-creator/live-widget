package org.plum.livewidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            saveMediaToInternal(uri)
        }
    }

    // Запрос разрешений для Android 13+
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(this, "Разрешения получены!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Нужны разрешения для доступа к файлам", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(50, 100, 50, 50)
            gravity = Gravity.CENTER
        }

        val title = TextView(this).apply {
            text = "Live Widget"
            textSize = 32f
            setTextColor(Color.parseColor("#4CAF50"))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val btnSelect = Button(this).apply {
            text = "Выбрать Медиа"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener {
                pickMedia.launch("*/*")
            }
        }

        val btnRefresh = Button(this).apply {
            text = "Обновить виджет"
            textSize = 16f
            setBackgroundColor(Color.parseColor("#2196F3"))
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener {
                updateWidget()
                Toast.makeText(this@MainActivity, "Виджет обновлен!", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(title)
        layout.addView(btnSelect)
        layout.addView(btnRefresh)
        setContentView(layout)
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissions = arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            val missing = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            if (missing.isNotEmpty()) {
                requestPermissionLauncher.launch(missing.toTypedArray())
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
            }
        }
    }

    private fun saveMediaToInternal(uri: Uri) {
        try {
            val mimeType = contentResolver.getType(uri) ?: ""
            val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
            val editor = prefs.edit()

            if (mimeType.startsWith("video/")) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, uri)
                val bitmap = retriever.getFrameAtTime(0)
                retriever.release()

                if (bitmap != null) {
                    val file = File(filesDir, "widget_media_video.jpg")
                    FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                    }
                    editor.putString("media_path", file.absolutePath)
                    editor.putBoolean("is_gif", false)
                    Toast.makeText(this, "Обложка видео сохранена!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val ext = if (mimeType.contains("gif")) "gif" else "jpg"
                val file = File(filesDir, "widget_media.$ext")
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                editor.putString("media_path", file.absolutePath)
                editor.putBoolean("is_gif", ext == "gif")
                Toast.makeText(this, "Файл сохранен!", Toast.LENGTH_SHORT).show()
            }

            editor.apply()
            updateWidget()
            Toast.makeText(this, "Добавьте виджет на рабочий стол.", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateWidget() {
        val intent = Intent(this, MediaWidgetProvider::class.java).apply {
            action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        val ids = android.appwidget.AppWidgetManager.getInstance(this).getAppWidgetIds(android.content.ComponentName(this, MediaWidgetProvider::class.java))
        intent.putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}
