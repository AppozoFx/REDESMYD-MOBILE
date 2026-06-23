package com.redes.app.data.tracking

import android.content.Context
import android.content.Intent
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TrackingManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("redes_tracking", Context.MODE_PRIVATE)

    fun startIfNeeded() {
        val todayYmd = todayLimaYmd()
        if (prefs.getString(KEY_RUTA_CERRADA_YMD, null) == todayYmd) return
        if (prefs.getString(KEY_LAST_START_YMD, null) == todayYmd) return
        prefs.edit().putString(KEY_LAST_START_YMD, todayYmd).apply()
        val intent = Intent(context, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stop() {
        prefs.edit().remove(KEY_LAST_START_YMD).apply()
        runCatching {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP
            }
            context.startService(intent)
        }
    }

    fun stopAndCloseRoute() {
        val todayYmd = todayLimaYmd()
        prefs.edit()
            .putString(KEY_RUTA_CERRADA_YMD, todayYmd)
            .remove(KEY_LAST_START_YMD)
            .apply()
        runCatching {
            val intent = Intent(context, LocationTrackingService::class.java).apply {
                action = LocationTrackingService.ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private fun todayLimaYmd(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("America/Lima")
        }.format(Date())
    }

    companion object {
        private const val KEY_LAST_START_YMD = "last_start_ymd"
        private const val KEY_RUTA_CERRADA_YMD = "ruta_cerrada_ymd"
    }
}
