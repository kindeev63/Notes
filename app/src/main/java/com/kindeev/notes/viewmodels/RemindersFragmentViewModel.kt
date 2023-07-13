package com.kindeev.notes.viewmodels

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.fragments.ReminderDialogFragment
import com.kindeev.notes.other.Action
import com.kindeev.notes.other.ReminderToShow
import com.kindeev.notes.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Locale

class RemindersFragmentViewModel : ViewModel() {
    private var _allReminders = emptyList<Reminder>()
    private val _remindersList = MutableLiveData<List<Reminder>>()
    val remindersList: LiveData<List<Reminder>> = _remindersList
    private val _selectedReminders = MutableLiveData<List<Reminder>>()
    val selectedReminders: LiveData<List<Reminder>> = _selectedReminders
    var searchText = ""
        set(value) {
            field = value
            filterReminders()
        }

    fun setAllReminders(reminders: List<Reminder>) {
        _allReminders = reminders
        filterReminders()
    }

    fun clearSelectedReminders() {
        _selectedReminders.value = emptyList()
    }

    private fun filterReminders() {
        _remindersList.value =
            _allReminders.filter { it.title.lowercase().contains(searchText.lowercase()) }
    }

    private fun openReminder(
        reminder: Reminder? = null, fragmentManager: FragmentManager
    ) {
        val dialogFragment = ReminderDialogFragment.newInstance(
            reminder = reminder
        )
        dialogFragment.show(fragmentManager, "reminder_dialog")
    }

    fun createReminder(
        activity: Activity, fragmentManager: FragmentManager
    ) {
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openReminder(
                fragmentManager = fragmentManager
            )
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }
    }

    fun onClickReminder(
        fragmentManager: FragmentManager, mainActivity: MainActivity
    ): (Reminder, Boolean) -> Unit = { reminder: Reminder, long: Boolean ->
        if (long) {
            if (selectedReminders.value?.contains(reminder) == true) {
                _selectedReminders.value =
                    ArrayList(_selectedReminders.value ?: emptyList()).apply {
                        remove(reminder)
                    }
            } else {
                _selectedReminders.value =
                    ArrayList(_selectedReminders.value ?: emptyList()).apply {
                        add(reminder)
                    }
            }
        } else {
            if (selectedReminders.value?.isEmpty() != false) {
                openReminder(
                    reminder = reminder, fragmentManager = fragmentManager
                )
            } else {
                if (selectedReminders.value?.contains(reminder) == true) {
                    _selectedReminders.value =
                        ArrayList(_selectedReminders.value ?: emptyList()).apply {
                            remove(reminder)
                        }
                } else {
                    _selectedReminders.value =
                        ArrayList(_selectedReminders.value ?: emptyList()).apply {
                            add(reminder)
                        }
                }
            }
        }
        if (selectedReminders.value?.isNotEmpty() != true) {
            mainActivity.getTopMenu()?.forEach {
                it.isVisible = it.itemId != R.id.delete_item
            }

        } else {
            mainActivity.getTopMenu()?.forEach {
                it.isVisible = it.itemId == R.id.delete_item || it.itemId == R.id.action_search
            }
        }
    }

    fun deleteReminders(mainAppViewModel: MainAppViewModel, context: Context) {
        selectedReminders.value?.let { selectedReminders ->
            selectedReminders.map { it.id }.forEach { reminderId ->
                cancelAlarm(reminderId, context)
            }
            mainAppViewModel.deleteReminders(selectedReminders)
            clearSelectedReminders()
        }
    }

    private fun cancelAlarm(reminderId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun toRemindersToShow(reminders: List<Reminder>, context: Context): List<ReminderToShow> {
        val remindersToShow = arrayListOf<ReminderToShow>()
        for (reminder in reminders) {
            remindersToShow.add(
                ReminderToShow(
                    title = reminder.title,
                    actionIcon = if (reminder.action == Action.OpenNote) {
                        context.resources.getDrawable(R.drawable.ic_bottom_notes)
                    } else {
                        val appInfo = context.packageManager.getApplicationInfo(
                            reminder.packageName, PackageManager.GET_META_DATA
                        )
                        context.packageManager.getApplicationIcon(appInfo)
                    },
                    time = getFormattedTime(reminder),
                    date = getFormattedDate(reminder),
                    soundIcon = if (reminder.sound) {
                        context.resources.getDrawable(R.drawable.ic_sound_on)
                    } else {
                        context.resources.getDrawable(R.drawable.ic_sound_off)
                    },
                    reminder = reminder
                )
            )
        }
        return remindersToShow
    }

    private fun getFormattedTime(reminder: Reminder): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
        reminder.time
    )

    private fun getFormattedDate(reminder: Reminder): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
        reminder.time
    )
}