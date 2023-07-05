package com.kindeev.notes.fragments

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentReminderDialogBinding
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.other.Action
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.viewmodels.ReminderDialogFragmentViewModel

class ReminderDialogFragment : DialogFragment() {
    private val viewModel: ReminderDialogFragmentViewModel by viewModels()
    private lateinit var binding: FragmentReminderDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val reminder = it.getSerializable("reminder") as Reminder?
            val noteId = if (it.containsKey("noteId")) it.getInt("noteId", 0) else null
            viewModel.setReminder(
                newReminder = reminder,
                noteId = noteId,
                mainViewModel = mainViewModel(),
                packageName = requireContext().packageName
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val timePicker = viewModel.timePicker { hour, minute ->
            viewModel.setReminderTime(
                hour = hour, minute = minute
            )
            binding.tTimeDialog.text = viewModel.getStringTime(hour, minute)
        }
        val datePicker = viewModel.datePicker { year, month, day ->
            viewModel.setReminderTime(
                year = year, month = month, day = day
            )
            binding.tDateDialog.text = viewModel.getStringDate(year, month, day)
        }

        binding.apply {
            tDateDialog.textSize = viewModel.dateTextSize(requireContext())
            tTimeDialog.textSize = viewModel.timeTextSize(requireContext())
            for (layoutParams in listOf(imageDateDialog, imageTimeDialog).map { it.layoutParams }) {
                layoutParams.width = viewModel.smallImageSize(requireContext())
                layoutParams.height = viewModel.smallImageSize(requireContext())
            }
            imageSoundType.layoutParams.apply {
                width = viewModel.mediumImageSize(requireContext())
                height = viewModel.mediumImageSize(requireContext())
            }
            tTimeDialog.text = viewModel.getFormattedTime()
            tDateDialog.text = viewModel.getFormattedDate()
            eTitleDialog.setText(viewModel.reminder?.title ?: "")
            eDescriptionDialog.setText(viewModel.reminder?.description ?: "")
            LinearTimeDialog.setOnClickListener {
                timePicker.show(childFragmentManager, "timePicker")
            }
            LinearDateDialog.setOnClickListener {
                datePicker.show(childFragmentManager, "datePicker")
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.openNoteButton -> {
                        if (mainViewModel().allNotes.value?.isEmpty() != false) {
                            openAppButton.isChecked = true
                            Toast.makeText(requireContext(), R.string.no_notes, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            viewModel.reminder?.action = Action.OpenNote
                            noteCardDialog.visibility = View.VISIBLE
                            appCardDialog.visibility = View.GONE
                        }

                    }

                    R.id.openAppButton -> {
                        viewModel.reminder?.action = Action.OpenApp
                        appCardDialog.visibility = View.VISIBLE
                        noteCardDialog.visibility = View.GONE
                    }
                }
            }

            noteContentDialog.setOnClickListener {
                viewModel.showListDialog(mainViewModel().allNotes.value ?: emptyList(), childFragmentManager) {
                    viewModel.reminder?.noteId = it.id
                    tNoteTitleDialog.text = it.title
                    noteContentDialog.setBackgroundColor(it.color)
                }

            }
            appContentDialog.setOnClickListener {
                viewModel.showAppsDialog(childFragmentManager) {
                    appNameDialog.text = it.name
                    appIconDialog.setImageDrawable(it.icon)
                    viewModel.reminder?.packageName = it.packageName
                }
            }
            imageSoundType.setOnClickListener {
                imageSoundType.setImageResource(if (viewModel.reminder?.sound != false) R.drawable.ic_sound_off else R.drawable.ic_sound_on)
                viewModel.changeReminderSoundType()
            }
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentReminderDialogBinding.inflate(layoutInflater)
        binding.apply {
            imageSoundType.setImageResource(if (viewModel.reminder?.sound != false) R.drawable.ic_sound_on else R.drawable.ic_sound_off)
            if (viewModel.reminder?.action != Action.OpenNote) {
                openAppButton.isChecked = true
                appCardDialog.visibility = View.VISIBLE
                noteCardDialog.visibility = View.GONE
                val appInfo = requireContext().packageManager.getApplicationInfo(
                    viewModel.reminder?.packageName ?: requireContext().packageName,
                    PackageManager.GET_META_DATA
                )
                appNameDialog.text = requireContext().packageManager.getApplicationLabel(appInfo)
                appIconDialog.setImageDrawable(
                    requireContext().packageManager.getApplicationIcon(appInfo)
                )
            } else {
                openNoteButton.isChecked = true
                noteCardDialog.visibility = View.VISIBLE
                appCardDialog.visibility = View.GONE
                mainViewModel().getNoteById(viewModel.reminder?.noteId!!) {
                    binding.tNoteTitleDialog.text = it?.title
                    binding.noteContentDialog.setBackgroundColor(it?.color ?: Color.WHITE)
                }
            }
        }
        return viewModel.makeDialog(requireContext(), binding.root) {
            if (viewModel.reminder?.action == Action.OpenNote && viewModel.reminder?.noteId == null) {
                Toast.makeText(requireContext(), R.string.pick_note_or_app, Toast.LENGTH_SHORT)
                    .show()
            } else {
                viewModel.saveReminder(
                    title = binding.eTitleDialog.text.toString(),
                    description = binding.eDescriptionDialog.text.toString(),
                    mainViewModel = mainViewModel(),
                    context = requireContext()
                )
                it.dismiss()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("reminder", viewModel.reminder)
    }

    private fun mainViewModel() = (requireContext().applicationContext as MainApp).mainViewModel

    companion object {
        @JvmStatic
        fun newInstance(
            reminder: Reminder?, noteId: Int? = null
        ): ReminderDialogFragment {
            return ReminderDialogFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("reminder", reminder)
                    if (noteId != null) putInt("noteId", noteId)
                }
            }
        }
    }
}