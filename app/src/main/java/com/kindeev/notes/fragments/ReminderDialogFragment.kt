package com.kindeev.notes.fragments

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.adapters.PickNotesAdapter
import com.kindeev.notes.databinding.FragmentReminderDialogBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class ReminderDialogFragment(val reminder: Reminder? = null, private val reminderId: Int, private val noteViewModel: NoteViewModel) : DialogFragment() {
    private lateinit var date: Calendar
    private lateinit var binding: FragmentReminderDialogBinding
    private var note: Note? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val timePicker =
            MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setHour(date[Calendar.HOUR_OF_DAY])
                .setMinute(date[Calendar.MINUTE]).setTitleText(R.string.select_time).build()
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
            LinearTimeDialog.setOnClickListener {
                timePicker.show(childFragmentManager, "timePicker")
            }
            LinearDateDialog.setOnClickListener {
                datePicker.show(childFragmentManager, "datePicker")
            }
            noteContentDialog.setOnClickListener {
                showListDialog(noteViewModel.allNotes.value?: emptyList()){
                    note = it
                    tNoteTitleDialog.text = it.title
                    noteContentDialog.setBackgroundColor(it.color)
                }
            }
        }

        return binding.root
    }
    private fun showListDialog(notes: List<Note>, listener: (Note) -> Unit){
        val recyclerView = RecyclerView(requireContext())
        val dialog = AlertDialog.Builder(requireContext()).apply {
            setTitle(resources.getString(R.string.pick_note))
            setView(recyclerView)
            setNegativeButton(R.string.cancel, null)

        }.create()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PickNotesAdapter(dialog, listener).apply {
            setData(notes)
        }

        dialog.show()
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
        date = Calendar.getInstance().apply {
            if (reminder!=null){
                timeInMillis = reminder.time
            }
        }
        if (reminder==null){
            note = null
        } else {
            noteViewModel.getNoteById(reminder.noteId){
                note = it!!
                binding.tNoteTitleDialog.text = it.title
                binding.noteContentDialog.setBackgroundColor(it.color)
            }
        }

        binding = FragmentReminderDialogBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (note==null){
                    Toast.makeText(requireContext(), R.string.not_select_a_note, Toast.LENGTH_SHORT).show()
                } else {
                    val newReminder = Reminder(reminderId, binding.eTitleDialog.text.toString(), date.timeInMillis, note!!.id)
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