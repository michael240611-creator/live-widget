package org.plum.livewidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import java.io.File

class MediaWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val prefs = context.getSharedPreferences("WidgetPrefs", Context.MODE_PRIVATE)
            val path = prefs.getString("media_path", null)
            val isGif = prefs.getBoolean("is_gif", false)

            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    if (isGif) {
                        views.setViewVisibility(R.id.widget_gif, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_image, View.GONE)
                        try {
                            // Отображаем только первый кадр GIF как статичную картинку
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            if (bitmap != null) {
                                views.setImageViewBitmap(R.id.widget_gif, bitmap)
                            }
                        } catch (e: Exception) { }
                    } else {
                        views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                        views.setViewVisibility(R.id.widget_gif, View.GONE)
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widget_image, bitmap)
                        }
                    }
                }
            } else {
                views.setViewVisibility(R.id.widget_image, View.GONE)
                views.setViewVisibility(R.id.widget_gif, View.GONE)
            }
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
