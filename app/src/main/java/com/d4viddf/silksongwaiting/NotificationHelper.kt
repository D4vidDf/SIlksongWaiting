package com.d4viddf.silksongwaiting

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import io.github.d4viddf.hyperisland_kit.HyperIslandNotification
import io.github.d4viddf.hyperisland_kit.HyperPicture
import io.github.d4viddf.hyperisland_kit.models.TimerInfo
import java.util.Calendar
import java.util.TimeZone

object NotificationHelper {
    const val CHANNEL_ID = "silksong_waiting_channel"
    const val NOTIFICATION_ID = 101
    private const val PIC_KEY_SILKSONG = "pic_silksong_logo"

    // Time: 12:43 AM UTC, December 16, 2025
    val ANNOUNCEMENT_TIME_MILLIS: Long by lazy {
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            set(2025, Calendar.DECEMBER, 16, 0, 43, 0) // 00:43 is 12:43 AM
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Silksong Counter"
            val descriptionText = "Shows how long we have been waiting"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showCountUpNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        // 1. Setup Resources
        val pic = HyperPicture(PIC_KEY_SILKSONG, context, R.drawable.ic_silksong)
        val startTime = ANNOUNCEMENT_TIME_MILLIS

        val countUpTimer = TimerInfo(
            timerType = 1,
            timerWhen = startTime,
            timerTotal = startTime,
            timerSystemCurrent = System.currentTimeMillis()
        )

        // 2. Build Hyper Params
        val hyperBuilder = HyperIslandNotification.Builder(context, "silksong_id", "Silksong")
            .addPicture(pic)
            .setChatInfo("Silksong SEA of SORROW", "Waiting...", PIC_KEY_SILKSONG, timer = countUpTimer, appPkg = "com.d4viddf.silksongwaiting")
            .setSmallIslandIcon(PIC_KEY_SILKSONG)
            .setReopen(true)
            .setPadding(true)
            .setIslandConfig(priority = 1, dismissible = true, expandedTimeMs = 5000, highlightColor = "#87DAE4", maxSize = true)
            .setBigIslandCountUp(startTime, PIC_KEY_SILKSONG)

        // 3. Generate Payloads
        val jsonPayload = hyperBuilder.buildJsonParam()
        val resBundle = hyperBuilder.buildResourceBundle()

        // 4. Create Standard Notification
        val notifBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setWhen(startTime)
            .setUsesChronometer(true)
            .setContentTitle("Silksong Counter")
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        // 5. Attach Extras
        val extras = Bundle()
        extras.putString("miui.focus.param", jsonPayload)
        extras.putAll(resBundle)
        notifBuilder.setExtras(extras)

        // 6. Notify
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            notificationManager.notify(NOTIFICATION_ID, notifBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }
}