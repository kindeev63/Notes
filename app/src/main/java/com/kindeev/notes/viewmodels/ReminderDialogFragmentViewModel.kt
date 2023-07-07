package com.kindeev.notes.viewmodels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kindeev.notes.R
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.fragments.PickAppFragment
import com.kindeev.notes.fragments.PickNoteFragment
import com.kindeev.notes.other.Action
import com.kindeev.notes.other.ApplicationData
import com.kindeev.notes.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ReminderDialogFragmentViewModel : ViewModel() {
    var reminder: Reminder? = null
    fun setReminder(
        newReminder: Reminder?,
        noteId: Int? = null,
        mainAppViewModel: MainAppViewModel,
        packageName: String
    ) {
        if (newReminder!=null) {
            reminder = newReminder
            return
        }
        val idsList = mainAppViewModel.allReminders.value?.map { it.id } ?: emptyList()
        var reminderId = 0
        while (reminderId in idsList) {
            reminderId++
        }
        reminder = Reminder(
            id = reminderId,
            title = "",
            description = "",
            time = Calendar.getInstance().timeInMillis,
            noteId = noteId,
            packageName = packageName,
            sound = true,
            action = if (noteId==null) Action.OpenApp else Action.OpenNote
        )
    }

    fun setReminderTime(
        year: Int? = null,
        month: Int? = null,
        day: Int? = null,
        hour: Int? = null,
        minute: Int? = null
    ) {
        val calendar = Calendar.getInstance().apply {
            reminder?.time?.let {
                timeInMillis = it
            }
        }
        year?.let { calendar[Calendar.YEAR] = it }
        month?.let { calendar[Calendar.MONTH] = it }
        day?.let { calendar[Calendar.DAY_OF_MONTH] = it }
        hour?.let { calendar[Calendar.HOUR_OF_DAY] = it }
        minute?.let { calendar[Calendar.MINUTE] = it }
        reminder?.time = calendar.timeInMillis
    }

    fun timePicker(onPick: (Int, Int) -> Unit): MaterialTimePicker {
        val calendar = Calendar.getInstance().apply {
            reminder?.time?.let {
                timeInMillis = it
            }
        }
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar[Calendar.HOUR_OF_DAY]).setMinute(calendar[Calendar.MINUTE])
            .setTitleText(R.string.select_time).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        timePicker.addOnPositiveButtonClickListener {
            onPick(timePicker.hour, timePicker.minute)
        }
        return timePicker
    }

    fun getStringTime(hour: Int, minute: Int) = "$hour:${if (minute < 10) "0${minute}" else minute}"

    fun datePicker(onPick: (Int, Int, Int) -> Unit): MaterialDatePicker<Long> {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(reminder?.time ?: Calendar.getInstance().timeInMillis).build()
        datePicker.addOnPositiveButtonClickListener { timeInMillis ->
            val calendar = Calendar.getInstance().apply {
                this.timeInMillis = timeInMillis
            }
            onPick(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            )
        }
        return datePicker
    }

    fun getStringDate(year: Int, month: Int, day: Int) =
        "${if (day < 10) "0${day}" else day}.${if (month < 10) "0${month}" else month}.$year"

    fun smallImageSize(context: Context) =
        (context.resources.displayMetrics.widthPixels * 0.1f).toInt()

    fun mediumImageSize(context: Context) =
        (context.resources.displayMetrics.widthPixels * 0.11f).toInt()

    fun timeTextSize(context: Context) = context.resources.displayMetrics.widthPixels * 0.035f

    fun dateTextSize(context: Context) = context.resources.displayMetrics.widthPixels * 0.025f

    fun getFormattedTime(): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(
        reminder?.time ?: Calendar.getInstance().timeInMillis
    )

    fun getFormattedDate(): String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
        reminder?.time ?: Calendar.getInstance().timeInMillis
    )

    fun changeReminderSoundType() {
        reminder?.sound = !(reminder?.sound ?: true)
    }

    fun makeDialog(context: Context, view: View, onPositiveButtonClickListener: (AlertDialog) -> Unit): AlertDialog {
        val dialog = AlertDialog.Builder(context).setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                onPositiveButtonClickListener(dialog)
            }
        }
        return dialog
    }
    fun saveReminder(title: String, description: String, mainAppViewModel: MainAppViewModel, context: Context) {
        reminder?.let {
            it.title = title
            it.description = description
            mainAppViewModel.insertReminder(it)
            setAlarm(it, context)
            Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setAlarm(reminder: Reminder, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder", reminder)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent
        )
    }

    fun showListDialog(fragmentManager: FragmentManager, listener: (Note) -> Unit) {
        val dialog = PickNoteFragment.newInstance(listener)
        dialog.show(fragmentManager, "pick_notes")
    }

    fun showAppsDialog(fragmentManager: FragmentManager, listener: (ApplicationData) -> Unit) {
        val dialog = PickAppFragment.newInstance(listener)
        dialog.show(fragmentManager, "pick_apps")
    }
}