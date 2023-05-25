package com.kindeev.notes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.RemindersAdapter
import com.kindeev.notes.databinding.FragmentRemindersBinding
import com.kindeev.notes.db.Reminder

class RemindersFragment : BaseFragment() {
    private lateinit var binding: FragmentRemindersBinding
    private lateinit var noteViewModel: NoteViewModel
    lateinit var remindersAdapter: RemindersAdapter
    private var remindersList = emptyList<Reminder>()
    private var searchText: String = ""

    override fun onClickNew() {
        openReminder()
    }

    override fun search(text: String) {
        searchText = text
        remindersList = filterReminders(noteViewModel.allReminders.value, searchText)
        remindersAdapter.setData(reminders = remindersList)
        binding.noReminders.visibility = if (remindersList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterReminders(
        remindersList: List<Reminder>?,
        searchText: String
    ): List<Reminder> {

        return remindersList?.filter { it.title.lowercase().contains(searchText.lowercase()) } ?: emptyList()
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
                    mainActivity.menu?.findItem(R.id.reminder_item)?.isVisible = false
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
        val idsList = noteViewModel.allReminders.value?.map { it.id } ?: emptyList()
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