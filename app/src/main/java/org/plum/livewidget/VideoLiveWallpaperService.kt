package org.plum.livewidget

import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.io.File

class VideoLiveWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    inner class VideoEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
            val path = prefs.getString("video_path", null)
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(file.absolutePath)
                        setSurface(holder.surface)
                        isLooping = true
                        setVolume(0f, 0f) // Без звука
                        prepare()
                        start()
                    }
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (mediaPlayer != null) {
                if (visible) mediaPlayer?.start() else mediaPlayer?.pause()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
