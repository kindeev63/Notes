package com.kindeev.notes.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.ReminderItemBinding
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.other.ReminderToShow

class RemindersAdapter(private val onItemClick: (reminder: Reminder, long: Boolean) -> Unit) :
    RecyclerView.Adapter<RemindersAdapter.RemindersHolder>() {
    private var remindersList = emptyList<ReminderToShow>()
    private var selectedRemindersList = emptyList<Reminder>()

    class RemindersHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ReminderItemBinding.bind(view)
        fun bind(reminderToShow: ReminderToShow, selectedReminders: List<Reminder>) =
            with(binding) {
                reminderTitle.text = reminderToShow.title
                reminderDescription.text = reminderToShow.description
                reminderTime.text = reminderToShow.time
                reminderDate.text = reminderToShow.date
                reminderAction.setImageDrawable(reminderToShow.actionIcon)
                reminderSound.setImageDrawable(reminderToShow.soundIcon)
                if (reminderToShow.reminder in selectedReminders) {
                    reminderPickingView.setBackgroundColor(Color.BLACK)
                } else {
                    reminderPickingView.setBackgroundColor(Color.TRANSPARENT)
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
            onItemClick(remindersList[position].reminder, false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(remindersList[position].reminder, true)
            return@setOnLongClickListener true
        }
    }

    fun setData(reminders: List<ReminderToShow>? = null, selectedReminders: List<Reminder>? = null) {
        reminders?.let { remindersList = reminders.sortedBy { it.time }.reversed() }
        selectedReminders?.let { selectedRemindersList = it }
        notifyDataSetChanged()
    }
}