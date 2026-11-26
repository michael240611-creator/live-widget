package org.plum.livewidget

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import java.io.File

class VideoOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var videoView: VideoView
    private lateinit var params: WindowManager.LayoutParams
    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
        val path = prefs.getString("video_path", null)

        if (path == null || !File(path).exists()) {
            stopSelf()
            return START_NOT_STICKY
        }

        videoView = VideoView(this).apply {
            setVideoURI(Uri.fromFile(File(path)))
            setOnPreparedListener { mp ->
                mediaPlayer = mp
                mp.isLooping = true
                mp.setVolume(0f, 0f) // Без звука
                start()
            }
        }

        // Размер окна (например, 200x200 пикселей)
        val size = 400 // Размер виджета на экране
        
        params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        // Логика перетаскивания окна
        videoView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(videoView, params)
                        return true
                    }
                }
                return false
            }
        })

        windowManager.addView(videoView, params)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::videoView.isInitialized) {
            windowManager.removeView(videoView)
            mediaPlayer?.release()
        }
    }
}
