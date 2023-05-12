package com.kindeev.notes.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.receivers.AlarmReceiver
import kotlinx.coroutines.*
import java.util.*

class UpdateService : Service() {
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var alarmManager: AlarmManager
    private lateinit var manager: NotificationManager
    private val scope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        noteViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(NoteViewModel::class.java)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, Notifications.CHANNEL_ID)
            .setContentTitle("Update Service")
            .setContentText("Updating reminders...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
        startForeground(101, notification)
            scope.launch {
                val allReminders = noteViewModel.getAllReminders() ?: emptyList()
                Log.e("test", "All reminders = $allReminders")
                val time = Calendar.getInstance().timeInMillis
                val showReminders = mutableListOf<Reminder>()
                val updateReminders = mutableListOf<Reminder>()
                for (reminder in allReminders) {
                    if (reminder.time <= time) {
                        showReminders.add(reminder)
                    } else {
                        updateReminders.add(reminder)
                    }
                }
                Log.e("test", "Updating = $updateReminders")
                Log.e("test", "Showing = $showReminders")
                for (reminder in updateReminders) {
                    val i = Intent(this@UpdateService, AlarmReceiver::class.java).apply {
                        putExtra("reminder", reminder)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        this@UpdateService,
                        reminder.id,
                        i,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent
                    )
                }
                Log.e("test", "Update Complete")
                if (showReminders.isNotEmpty()){
                    makeAllNotifications(showReminders)
                    noteViewModel.deleteReminders(showReminders)
                    Log.e("test", "Show Complete")
                }
                val notificationIntent = Intent(this@UpdateService, NoteActivity::class.java)
                notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val builder =
                    NotificationCompat.Builder(this@UpdateService, Notifications.CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Update Completed!")
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                manager.notify(0, builder.build())
                stopForeground(true)
                stopSelf()
            }
        return START_NOT_STICKY
    }

    private fun makeAllNotifications(reminders: List<Reminder>){
        for (reminder in reminders){
            createNotification(this@UpdateService, reminder.title, reminder.id, reminder.noteId)
        }
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "MyApp:MyWakeLock")
        wakeLock.acquire(5000)
    }
    @SuppressLint("MissingPermission")
    private fun createNotification(context:Context, title: String, reminderId:Int, noteId: Int){
        val notificationIntent = Intent(context, NoteActivity::class.java).apply {
            putExtra("noteId", noteId)
        }
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
