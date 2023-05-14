package com.kindeev.notes.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.ReminderDialogFragment
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.PickNotesAdapter
import com.kindeev.notes.adapters.RemindersAdapter
import com.kindeev.notes.databinding.FragmentRemindersBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.receivers.AlarmReceiver
import java.util.*

class RemindersFragment : BaseFragment() {
    private lateinit var binding: FragmentRemindersBinding
    private lateinit var noteViewModel: NoteViewModel
    lateinit var remindersAdapter: RemindersAdapter
    private var remindersList = emptyList<Reminder>()
    var searchText: String = ""

    override fun onClickNew() {
        openReminder()
    }

    override fun search(text: String) {

    }

    private fun filterReminders(
        remindersList: List<Reminder>?,
        searchText: String
    ): List<Reminder> {

        return remindersList?.filter { it.title.contains(searchText) } ?: emptyList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()

        val onClickReminder: (Reminder, Boolean) -> Unit =
            { reminder: Reminder, open: Boolean ->
                val mainActivity = activity as MainActivity
                if (open) {
                    openReminder(reminder)
                } else {
                    if (noteViewModel.selectedReminders.size == 0) {
                        val searchItem = mainActivity.menu?.findItem(R.id.action_search)
                        val searchView = searchItem?.actionView as SearchView
                        searchView.setQuery("", false)
                        searchView.isIconified = true
                        searchItem.collapseActionView()
                        mainActivity.menu?.forEach {
                            it.isVisible = it.itemId != R.id.delete_item
                        }

                    } else {
                        mainActivity.menu?.forEach {
                            it.isVisible =
                                it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                        }
                    }
                }

            }

        remindersAdapter = RemindersAdapter(noteViewModel, onClickReminder)
        binding.apply {
            rcReminders.adapter = remindersAdapter
            rcReminders.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allReminders.observe(requireActivity()) {
            remindersList = filterReminders(it, searchText)
            remindersAdapter.setData(reminders = remindersList)
            binding.noReminders.visibility = if (remindersList.isEmpty()) View.VISIBLE else View.GONE
        }

        return binding.root
    }

    private fun openReminder(reminder: Reminder? = null) {
        if ((noteViewModel.allNotes.value?: emptyList()).isEmpty()){
            Toast.makeText(requireContext(), R.string.no_notes, Toast.LENGTH_SHORT).show()
        }
        val idsList = remindersList.map { it.id }
        var reminderId = 0
        if (reminder==null){
            while (true) {
                if (reminderId !in idsList) break
                reminderId++
            }
        } else {
            reminderId = reminder.id
        }
        val dialogFragment = ReminderDialogFragment.newInstance(reminder, reminderId, noteViewModel)
        dialogFragment.show(childFragmentManager, "reminder_dialog")
    }

    companion object {

        @JvmStatic
        fun newInstance() = RemindersFragment()
    }
}