package org.plum.livewidget

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
    
    private val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            saveMediaToInternal(uri)
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
            text = "Live Widget"
            textSize = 32f
            setTextColor(Color.parseColor("#4CAF50"))
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
        }

        val btnSelect = Button(this).apply {
            text = "Выбрать Фото или GIF"
            textSize = 18f
            setBackgroundColor(Color.parseColor("#4CAF50"))
            setTextColor(Color.WHITE)
            setTypeface(typeface, Typeface.BOLD)
            setOnClickListener {
                pickMedia.launch("image/*")
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

    private fun saveMediaToInternal(uri: Uri) {
        try {
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val ext = if (mimeType.contains("gif")) "gif" else "jpg"
            
            val file = File(filesDir, "widget_media.$ext")
            contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            val prefs = getSharedPreferences("WidgetPrefs", MODE_PRIVATE)
            prefs.edit().apply {
                putString("media_path", file.absolutePath)
                putBoolean("is_gif", ext == "gif")
                apply()
            }

            updateWidget()
            Toast.makeText(this, "Файл сохранен! Добавьте виджет на рабочий стол.", Toast.LENGTH_LONG).show()
            
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
