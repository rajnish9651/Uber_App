
package com.trainee.project.uberapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class BookingReceiver : BroadcastReceiver() {

    private val channelId = "booking_channel"
    private val channelName = "Booking Requests"

    override fun onReceive(context: Context, intent: Intent) {
        val fromLocation = intent.getStringExtra("fromLocation")
        val toLocation = intent.getStringExtra("toLocation")

        if (fromLocation != null && toLocation != null) {
            showBookingNotification(context, fromLocation, toLocation)
        }

    }

    private fun showBookingNotification(context: Context, fromLocation: String, toLocation: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("New Ride Request")
            .setContentText("From: $fromLocation\nTo: $toLocation")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
    }

}