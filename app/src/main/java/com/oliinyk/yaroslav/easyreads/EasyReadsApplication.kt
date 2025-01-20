package com.oliinyk.yaroslav.easyreads

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.oliinyk.yaroslav.easyreads.domain.util.AppConstants.READ_TIME_COUNTER_NOTIFICATION_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EasyReadsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                READ_TIME_COUNTER_NOTIFICATION_CHANNEL_ID,
                getString(R.string.read_time_counter_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}