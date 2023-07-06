package com.kindeev.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.ReminderItemBinding
import com.kindeev.notes.db.Reminder
import java.text.SimpleDateFormat
import java.util.*

class RemindersAdapter(private val onItemClick: (reminder: Reminder, long: Boolean) -> Unit) :
    RecyclerView.Adapter<RemindersAdapter.RemindersHolder>() {
    private var remindersList = emptyList<Reminder>()
    private var selectedRemindersList = emptyList<Reminder>()

    class RemindersHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ReminderItemBinding.bind(view)
        fun bind(reminder: Reminder, selectedReminders: List<Reminder>) =
            with(binding) {
                tReminderTitle.text = reminder.title
                val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                tReminderTime.text = timeFormat.format(reminder.time)
                tReminderDate.text = dateFormat.format(reminder.time)
                chDeleteReminder.visibility =
                    if (selectedReminders.isEmpty()) {
                        View.GONE
                    } else {
                        chDeleteReminder.isChecked = reminder in selectedReminders
                        View.VISIBLE
                    }

            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RemindersHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.reminder_item, parent, false)
    )

    override fun getItemCount() = remindersList.size

    override fun onBindViewHolder(holder: RemindersHolder, position: Int) {
        holder.bind(
            remindersList[position],
            selectedRemindersList
        )
        holder.itemView.setOnClickListener {
            onItemClick(remindersList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(remindersList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(reminders: List<Reminder>? = null, selectedReminders: List<Reminder>? = null) {
        reminders?.let { remindersList = reminders.sortedBy { it.time }.reversed() }
        selectedReminders?.let { selectedRemindersList = it }
        notifyDataSetChanged()
    }
}