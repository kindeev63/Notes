package com.kindeev.notes.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.TaskItemBinding
import com.kindeev.notes.db.Task

class TasksAdapter(
    private val onTextClick: (task: Task, long: Boolean) -> Unit,
    private val onCheckBoxClick: (task: Task) -> Unit
) :
    RecyclerView.Adapter<TasksAdapter.TasksHolder>() {
    private var tasksList = emptyList<Task>()
    class TasksHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = TaskItemBinding.bind(view)
        fun bind(task: Task) = with(binding) {
            taskTitle.text = task.title
            taskTitle.paintFlags = if (task.done) {
                taskTitle.setTextColor(Color.GRAY)
                taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                taskTitle.setTextColor(Color.BLACK)
                taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            taskContent.setBackgroundColor(task.color)
            taskDone.isChecked = task.done

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TasksHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
    )

    override fun getItemCount() = tasksList.size

    override fun onBindViewHolder(holder: TasksHolder, position: Int) {
        holder.bind(tasksList[position])
        holder.binding.taskDoneLayout.setOnClickListener {
            onCheckBoxClick(tasksList[position])
        }
        holder.binding.taskTitle.setOnClickListener {
            onTextClick(tasksList[position], false)
        }
        holder.binding.taskTitle.setOnLongClickListener {
            onTextClick(tasksList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(tasks: List<Task>) {
        tasksList = tasks
        notifyDataSetChanged()
    }
}