package com.kindeev.notes.receivers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kindeev.notes.MainApp
import com.kindeev.notes.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteViewModel = (context.applicationContext as MainApp).noteViewModel
        val reminder = intent.getSerializableExtra("reminder") as Reminder
        createNotification(context, intent.getStringExtra("title")?: "Empty", reminder.id, reminder.noteId)
        noteViewModel.deleteReminders(listOf(reminder))

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:MyWakeLock")
        wakeLock.acquire(5000)
    }
    @SuppressLint("MissingPermission")
    private fun createNotification(context:Context, title: String, reminderId:Int, noteId: Int){
        val notificationIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra("noteId", noteId)
        }
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context, reminderId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val manager = NotificationManagerCompat.from(context)
        manager.notify(reminderId, builder.build())
    }
}