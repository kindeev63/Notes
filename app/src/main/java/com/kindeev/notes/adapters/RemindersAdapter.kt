package com.kindeev.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.ReminderItemBinding
import com.kindeev.notes.db.Reminder
import java.text.SimpleDateFormat
import java.util.*

class RemindersAdapter(private val noteViewModel: NoteViewModel, private val onItemClick: (reminder: Reminder, open: Boolean) -> Unit) :
    RecyclerView.Adapter<RemindersAdapter.RemindersHolder>() {
    private var remindersList = emptyList<Reminder>()
    class RemindersHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ReminderItemBinding.bind(view)
        fun bind(reminder: Reminder, choosingNotes: Boolean, reminderSelected: Boolean) = with(binding) {
            tReminderTitle.text = reminder.title
            val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            tReminderTime.text = timeFormat.format(reminder.time)
            tReminderDate.text = dateFormat.format(reminder.time)
            chDeleteReminder.visibility =
            if (choosingNotes){
                View.VISIBLE
            } else View.GONE
            chDeleteReminder.isChecked = reminderSelected

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RemindersHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
    )

    override fun getItemCount() = remindersList.size

    override fun onBindViewHolder(holder: RemindersHolder, position: Int) {
        holder.bind(remindersList[position], noteViewModel.selectedReminders.size > 0, remindersList[position] in noteViewModel.selectedReminders)
        holder.itemView.setOnClickListener {
            if (!States.reminderEdited) {
                val open = noteViewModel.selectedReminders.size == 0
                if (noteViewModel.selectedReminders.size > 0){
                    val reminder = remindersList[position]
                    if (reminder in noteViewModel.selectedReminders) noteViewModel.selectedReminders.remove(reminder)
                    else noteViewModel.selectedReminders.add(reminder)
                    notifyDataSetChanged()
                }
                onItemClick(remindersList[position], open)
            }

        }
        holder.itemView.setOnLongClickListener {
            if (!States.reminderEdited){
                val reminder = remindersList[position]
                if (reminder in noteViewModel.selectedReminders) noteViewModel.selectedReminders.remove(reminder)
                else noteViewModel.selectedReminders.add(reminder)
                notifyDataSetChanged()
                onItemClick(reminder, false)

            }
            return@setOnLongClickListener true
        }
    }

    fun setData(reminders: List<Reminder>? = null) {
        remindersList = reminders ?: remindersList
        notifyDataSetChanged()
    }
}