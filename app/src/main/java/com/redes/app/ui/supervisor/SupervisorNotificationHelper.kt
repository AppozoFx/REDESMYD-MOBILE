package com.redes.app.ui.supervisor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.redes.app.R
import java.util.concurrent.atomic.AtomicInteger

object SupervisorNotificationHelper {

    private const val CHANNEL_ID = "redes_supervisor_alertas"
    private val notifIdCounter = AtomicInteger(2000)

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas de Supervisor",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Alertas de garantías y órdenes asignadas al supervisor"
                enableVibration(true)
            }
            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

    fun postAlerta(context: Context, titulo: String, mensaje: String) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify(notifIdCounter.getAndIncrement(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not yet granted at runtime (Android 13+)
        }
    }
}
