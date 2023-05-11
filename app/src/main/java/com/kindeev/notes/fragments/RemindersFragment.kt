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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.R
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
        val c = Calendar.getInstance()
        if (reminder!=null) c.timeInMillis = reminder.time
        c[Calendar.SECOND] = 0
        val listenerTime = TimePickerDialog.OnTimeSetListener() { _, hour, minute ->
            c[Calendar.HOUR_OF_DAY] = hour
            c[Calendar.MINUTE] = minute

            // После выбора времени выполняется код здесь
            showEditDialog(title = resources.getString(R.string.enter_title), textOk = resources.getString(R.string.add), textCancel = resources.getString(R.string.cancel), reminderTitle = reminder?.title ?: "") { reminderTitle ->
                showListDialog(noteViewModel.allNotes.value ?: emptyList()) { note ->
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

                    val reminder = Reminder(
                        reminderId,
                        reminderTitle,
                        c.timeInMillis,
                        note.id,
                    )
                    noteViewModel.insertReminder(reminder)
                    setAlarm(reminder)
                }
            }

        }
        val listenerDate = DatePickerDialog.OnDateSetListener() { _, year, month, day ->
            c[Calendar.YEAR] = year
            c[Calendar.MONTH] = month
            c[Calendar.DAY_OF_MONTH] = day
            TimePickerDialog(
                requireContext(),
                listenerTime,
                c[Calendar.HOUR_OF_DAY],
                c[Calendar.MINUTE],
                true
            ).show()
        }
        DatePickerDialog(
            requireContext(),
            listenerDate,
            c[Calendar.YEAR],
            c[Calendar.MONTH],
            c[Calendar.DAY_OF_MONTH]
        ).show()
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        reminderTitle: String,
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            val input = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                setText(reminderTitle)
            }
            setView(input)
            setPositiveButton(textOk) { _, _ ->
                val text = input.text.toString()
                result(text)
            }
            setNegativeButton(textCancel) { d, _ -> d.cancel() }
            show()
        }
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
        fun newInstance() = RemindersFragment()
    }
}