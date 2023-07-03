package com.kindeev.notes.fragments

import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentReminderDialogBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.receivers.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class ReminderDialogFragment : DialogFragment() {
    private lateinit var date: Calendar
    private lateinit var binding: FragmentReminderDialogBinding
    private var packageName: String? = null
    private var sound = true
    private var reminder: Reminder? = null
    private var reminderId: Int = 0
    private lateinit var mainViewModel: MainViewModel
    private var noteId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey("reminder")) reminder = it.getSerializable("reminder") as Reminder
            reminderId = it.getInt("reminderId", 0)
            mainViewModel = it.getSerializable("noteViewModel") as MainViewModel
            if (it.containsKey("noteId")) noteId = it.getInt("noteId", 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(date[Calendar.HOUR_OF_DAY]).setMinute(date[Calendar.MINUTE])
            .setTitleText(R.string.select_time).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
            .build()
        val datePicker =
            MaterialDatePicker.Builder.datePicker().setSelection(date.timeInMillis).build()
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

            val screenWidth = resources.displayMetrics.widthPixels
            val textSize1 = screenWidth * 0.025f
            val textSize2 = screenWidth * 0.035f
            val imageSize1 = (screenWidth * 0.1f).toInt()
            val imageSize2 = (screenWidth * 0.11f).toInt()
            tDateDialog.textSize = textSize1
            tTimeDialog.textSize = textSize2
            imageDateDialog.layoutParams.apply {
                width = imageSize1
                height = imageSize1
            }
            imageTimeDialog.layoutParams.apply {
                width = imageSize1
                height = imageSize1
            }
            imageSoundType.layoutParams.apply {
                width = imageSize2
                height = imageSize2
            }

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
                when (checkedId) {
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
                if (mainViewModel.allNotes.value?.isEmpty() != false) {
                    Toast.makeText(requireContext(), R.string.no_notes, Toast.LENGTH_SHORT).show()
                } else {
                    showListDialog(mainViewModel.allNotes.value ?: emptyList()) {
                        noteId = it.id
                        tNoteTitleDialog.text = it.title
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
            imageSoundType.setOnClickListener {
                imageSoundType.setImageResource(if (sound) R.drawable.ic_sound_off else R.drawable.ic_sound_on)
                sound = !sound
            }
        }

        return binding.root
    }

    private fun showListDialog(notes: List<Note>, listener: (Note) -> Unit) {
        val dialog = PickNoteFragment.newInstance(notes, listener)
        dialog.show(childFragmentManager, "pick_notes")
    }

    private fun showAppsDialog(listener: (ApplicationInfo) -> Unit) {
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
            if (reminder != null) {
                timeInMillis = reminder!!.time
            }
        }
        binding.apply {
            sound = reminder?.sound ?: true
            imageSoundType.setImageResource(if (sound) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
            if (reminder?.noteId == null && noteId == null) {
                openAppButton.isChecked = true
                appCardDialog.visibility = View.VISIBLE
                noteCardDialog.visibility = View.GONE
                val appInfo = requireContext().packageManager.getApplicationInfo(
                    reminder?.packageName ?: requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
                appNameDialog.text = requireContext().packageManager.getApplicationLabel(appInfo)
                appIconDialog.setImageDrawable(
                    requireContext().packageManager.getApplicationIcon(
                        appInfo
                    )
                )
            } else {
                openNoteButton.isChecked = true
                noteCardDialog.visibility = View.VISIBLE
                appCardDialog.visibility = View.GONE
                mainViewModel.getNoteById(reminder?.noteId ?: noteId!!) {
                    noteId = it!!.id
                    binding.tNoteTitleDialog.text = it.title
                    binding.noteContentDialog.setBackgroundColor(it.color)
                }
            }
        }
        val dialog = AlertDialog.Builder(requireContext()).setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                if (binding.openNoteButton.isChecked && noteId == null) {
                    Toast.makeText(requireContext(), R.string.pick_note_or_app, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val newReminder = Reminder(
                        reminderId,
                        binding.eTitleDialog.text.toString(),
                        binding.eDescriptionDialog.text.toString(),
                        date.timeInMillis,
                        null,
                        null,
                        sound
                    )
                    if (binding.openAppButton.isChecked) {
                        newReminder.packageName = packageName ?: requireContext().packageName
                    } else {
                        newReminder.noteId = noteId
                    }
                    mainViewModel.insertReminder(newReminder)
                    setAlarm(newReminder)
                    Toast.makeText(requireContext(), R.string.saved, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }
        return dialog
    }

    private fun setAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("reminder", reminder)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, reminder.time, pendingIntent
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем состояние фрагмента
        if (reminder!=null) outState.putSerializable("reminder", reminder)
        outState.putInt("reminderId", reminderId)
        outState.putSerializable("noteViewModel", mainViewModel)
        if (noteId != null) outState.putInt("noteId", noteId!!)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            reminder: Reminder?, reminderId: Int, mainViewModel: MainViewModel, noteId: Int? = null
        ): ReminderDialogFragment {
            val fragment = ReminderDialogFragment()
            val args = Bundle()
            if (reminder != null) args.putSerializable("reminder", reminder)
            args.putInt("reminderId", reminderId)
            args.putSerializable("noteViewModel", mainViewModel)
            if (noteId != null) args.putInt("noteId", noteId)
            fragment.arguments = args
            return fragment
        }
    }
}