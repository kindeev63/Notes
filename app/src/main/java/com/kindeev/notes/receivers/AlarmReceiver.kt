package com.kindeev.notes.receivers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.other.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.db.Reminder

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteViewModel = (context.applicationContext as MainApp).noteViewModel
        val reminder = intent.getSerializableExtra("reminder") as Reminder
        createNotification(context, reminder.title, reminder.description, reminder.id, reminder.noteId, reminder.packageName)
        noteViewModel.deleteReminders(listOf(reminder))

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:MyWakeLock")
        wakeLock.acquire(5000)
    }
    @SuppressLint("MissingPermission")
    private fun createNotification(context:Context, title: String, description: String, reminderId:Int, noteId: Int?, packageName: String?){
        val notificationIntent = if (noteId==null) context.packageManager.getLaunchIntentForPackage(packageName!!) else Intent(context, NoteActivity::class.java).apply {
            putExtra("noteId", noteId)
        }
        notificationIntent?.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context, reminderId, notificationIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(context, Notifications.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(description)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        val manager = NotificationManagerCompat.from(context)
        manager.notify(reminderId, builder.build())
    }
}