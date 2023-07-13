package com.kindeev.notes.adapters

import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.CompoundButtonCompat
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.TaskItemBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors


class TasksAdapter(
    private val onTextClick: (task: Task, long: Boolean) -> Unit,
    private val onCheckBoxClick: (task: Task) -> Unit
) :
    RecyclerView.Adapter<TasksAdapter.TasksHolder>() {
    private var tasksList = emptyList<Task>()
    private var selectedTasksList = emptyList<Task>()

    class TasksHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = TaskItemBinding.bind(view)
        fun bind(task: Task, selectedTasks: List<Task>) = with(binding) {
            taskTitle.text = task.title
            taskTitle.paintFlags = if (task.done) {
                taskTitle.setTextColor(Color.GRAY)
                taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                taskTitle.setTextColor(Color.BLACK)
                taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            CompoundButtonCompat.getButtonDrawable(binding.taskCheckBox)?.colorFilter =
                PorterDuffColorFilter(
                    Colors.colors[task.colorIndex].primary,
                    PorterDuff.Mode.SRC_ATOP
                )
            taskCheckBox.isChecked = task.done
            if (task in selectedTasks) {
                taskPickingView.setBackgroundColor(Color.BLACK)
            } else {
                taskPickingView.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TasksHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
    )

    override fun getItemCount() = tasksList.size

    override fun onBindViewHolder(holder: TasksHolder, position: Int) {
        holder.bind(
            tasksList[position],
            selectedTasksList
        )
        holder.binding.taskDone.setOnClickListener {
            onCheckBoxClick(tasksList[position])
        }
        holder.itemView.setOnClickListener {
            onTextClick(tasksList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onTextClick(tasksList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(tasks: List<Task>? = null, selectedTasks: List<Task>? = null) {
        tasks?.let { tasksList = it }
        selectedTasks?.let { selectedTasksList = it }
        notifyDataSetChanged()
    }
}