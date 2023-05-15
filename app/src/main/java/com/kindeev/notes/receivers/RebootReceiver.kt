package com.kindeev.notes.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.kindeev.notes.other.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.services.UpdateService

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, UpdateService::class.java)
        val notification = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setContentTitle("Update Reminders")
            .setContentText("Updating reminders...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        val id = 111
        ContextCompat.startForegroundService(context, serviceIntent)
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.notify(id, notification)
    }

}