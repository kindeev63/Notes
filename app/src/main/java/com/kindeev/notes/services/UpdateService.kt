package com.kindeev.notes.services

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.db.Note
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
        GlobalScope.launch {
            val allReminders = withContext(Dispatchers.IO) {
                noteViewModel.getAllReminders() ?: emptyList()
            }
            scope.launch {
                Log.e("test", "All reminders = $allReminders")
                val time = Calendar.getInstance().timeInMillis
                val deleted = mutableListOf<Reminder>()
                val updated = mutableListOf<Reminder>()
                for (reminder in allReminders) {
                    if (reminder.time <= time) {
                        deleted.add(reminder)
                    } else {
                        updated.add(reminder)
                    }
                }
                Log.e("test", "Updated = $updated")
                Log.e("test", "Deleted = $deleted")
                for (reminder in updated) {
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
                noteViewModel.deleteReminders(deleted)
                Log.e("test", "Delete Complete")

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
        }
        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}
