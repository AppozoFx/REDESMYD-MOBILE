package com.redes.app.data.tracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.redes.app.REDESApplication
import com.redes.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationTrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var isTracking = false
    private var lastPostedLat: Double? = null
    private var lastPostedLng: Double? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            if (!movedEnough(location)) return
            val trackingRepository = (application as REDESApplication).appContainer.trackingRepository
            scope.launch {
                try {
                    trackingRepository.postLocation(
                        lat = location.latitude,
                        lng = location.longitude,
                        accuracy = if (location.hasAccuracy()) location.accuracy else null,
                        speed = if (location.hasSpeed()) location.speed else null,
                    )
                    lastPostedLat = location.latitude
                    lastPostedLng = location.longitude
                } catch (e: Exception) {
                    Log.w(TAG, "Error posting location", e)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                stopSelf()
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startTracking() {
        if (isTracking) return
        isTracking = true
        startForeground(NOTIFICATION_ID, buildNotification())
        val request = LocationRequest.Builder(INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .setMaxUpdateDelayMillis(MAX_DELAY_MS)
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()
        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
    }

    private fun movedEnough(location: Location): Boolean {
        val lastLat = lastPostedLat ?: return true
        val lastLng = lastPostedLng ?: return true
        val results = FloatArray(1)
        Location.distanceBetween(lastLat, lastLng, location.latitude, location.longitude, results)
        return results[0] >= MIN_DISTANCE_METERS
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        scope.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    override fun onBind(intent: Intent?) = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracking de ubicacion",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Registra tu posicion durante la jornada de trabajo" }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("REDES - En jornada")
            .setContentText("Registrando ubicacion")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_START = "com.redes.app.tracking.ACTION_START"
        const val ACTION_STOP = "com.redes.app.tracking.ACTION_STOP"
        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "redes_tracking"
        private const val INTERVAL_MS = 60_000L         // desired update: 1 min
        private const val MIN_INTERVAL_MS = 30_000L     // fastest: 30 sec when moving
        private const val MAX_DELAY_MS = 120_000L       // max wait before forced delivery
        private const val MIN_DISTANCE_METERS = 30f     // skip backend POST if didn't move 30m
        private const val TAG = "LocationTrackingService"
    }
}
