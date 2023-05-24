package com.kindeev.notes.fragments

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.adapters.PickNotesAdapter
import com.kindeev.notes.databinding.FragmentReminderDialogBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class ReminderDialogFragment(val reminder: Reminder?, private val reminderId: Int, private val noteViewModel: NoteViewModel) : DialogFragment() {
    private lateinit var date: Calendar
    private lateinit var binding: FragmentReminderDialogBinding
    private var noteId: Int? = null
    private var packageName: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val timePicker =
            MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(date[Calendar.HOUR_OF_DAY])
                .setMinute(date[Calendar.MINUTE]).setTitleText(R.string.select_time).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).build()
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(date.timeInMillis).build()
        timePicker.addOnPositiveButtonClickListener {
            val hour = timePicker.hour.toString()
            val minute =
                if (timePicker.minute < 10) "0${timePicker.minute}" else timePicker.minute.toString()
            binding.tTimeDialog.text = "$hour:$minute"
            date[Calendar.HOUR_OF_DAY] = timePicker.hour
            date[Calendar.MINUTE] = timePicker.minute
        }
        datePicker.addOnPositiveButtonClickListener {
            val formatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            binding.tDateDialog.text = formatter.format(it)
            val newDate = Calendar.getInstance().apply {
                timeInMillis = it
            }
            date[Calendar.YEAR] = newDate[Calendar.YEAR]
            date[Calendar.MONTH] = newDate[Calendar.MONTH]
            date[Calendar.DAY_OF_MONTH] = newDate[Calendar.DAY_OF_MONTH]
        }
        binding.apply {
            val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val formatterDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            tTimeDialog.text = formatterTime.format(date.timeInMillis)
            tDateDialog.text = formatterDate.format(date.timeInMillis)
            eTitleDialog.setText(reminder?.title ?: "")
            eDescriptionDialog.setText(reminder?.description ?: "")
            LinearTimeDialog.setOnClickListener {
                timePicker.show(childFragmentManager, "timePicker")
            }
            LinearDateDialog.setOnClickListener {
                datePicker.show(childFragmentManager, "datePicker")
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.openNoteButton -> {
                        noteCardDialog.visibility = View.VISIBLE
                        appCardDialog.visibility = View.GONE
                    }
                    R.id.openAppButton -> {
                        appCardDialog.visibility = View.VISIBLE
                        noteCardDialog.visibility = View.GONE
                    }
                }
            }

            noteContentDialog.setOnClickListener {
                if (noteViewModel.allNotes.value?.isEmpty() != false){
                    Toast.makeText(requireContext(), R.string.no_notes, Toast.LENGTH_SHORT).show()
                } else {
                    showListDialog(noteViewModel.allNotes.value?: emptyList()){
                        noteId = it.id
                        tNoteTitleDialog.text = it.title
                        tNoteTimeDialog.text = it.time
                        noteContentDialog.setBackgroundColor(it.color)
                    }
                }

            }
            appContentDialog.setOnClickListener {
                showAppsDialog {
                    appIconDialog.setImageDrawable(it.loadIcon(requireContext().packageManager))
                    appNameDialog.text = it.loadLabel(requireContext().packageManager)
                    packageName = it.packageName
                }
            }
        }

        return binding.root
    }
    private fun showListDialog(notes: List<Note>, listener: (Note) -> Unit){
        val dialog = PickNoteFragment.newInstance(notes, listener)
        dialog.show(childFragmentManager, "pick_notes")
    }
    private fun showAppsDialog(listener: (ApplicationInfo) -> Unit){
        val dialog = PickAppFragment.newInstance(listener)
        dialog.show(childFragmentManager, "pick_apps")
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = resources.displayMetrics.widthPixels
            dialog.window?.setLayout((width / 1.2).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentReminderDialogBinding.inflate(layoutInflater)
        date = Calendar.getInstance().apply {
            if (reminder!=null){
                timeInMillis = reminder.time
            }
        }
        binding.apply {
            if (reminder?.noteId == null) {
                openAppButton.isChecked = true
                appCardDialog.visibility = View.VISIBLE
                noteCardDialog.visibility = View.GONE
                val appInfo = requireContext().packageManager.getApplicationInfo(reminder?.packageName?: requireContext().packageName, PackageManager.GET_META_DATA)
                appNameDialog.text = requireContext().packageManager.getApplicationLabel(appInfo)
                appIconDialog.setImageDrawable(requireContext().packageManager.getApplicationIcon(appInfo))
            } else {
                openNoteButton.isChecked = true
                noteCardDialog.visibility = View.VISIBLE
                appCardDialog.visibility = View.GONE
                noteViewModel.getNoteById(reminder.noteId!!) {
                    noteId = it!!.id
                    binding.tNoteTitleDialog.text = it.title
                    binding.tNoteTimeDialog.text = it.time
                    binding.noteContentDialog.setBackgroundColor(it.color)
                }
            }
        }
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (binding.openNoteButton.isChecked && noteId==null){
                    Toast.makeText(requireContext(), R.string.pick_note_or_app, Toast.LENGTH_SHORT).show()
                } else {
                    val newReminder = Reminder(reminderId, binding.eTitleDialog.text.toString(), binding.eDescriptionDialog.text.toString(), date.timeInMillis, null, null)
                    if (binding.openAppButton.isChecked){
                        newReminder.packageName = packageName
                    } else {
                        newReminder.noteId = noteId
                    }
                    noteViewModel.insertReminder(newReminder)
                    setAlarm(newReminder)
                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }
    private fun setAlarm(reminder: Reminder){
        val alarmManager =
            requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("reminder", reminder)
        }

        val pendingIntent =
            PendingIntent.getBroadcast(requireContext(), reminder.id, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.time,
            pendingIntent
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(reminder: Reminder?, reminderId: Int, noteViewModel: NoteViewModel) = ReminderDialogFragment(reminder, reminderId, noteViewModel)
    }
}