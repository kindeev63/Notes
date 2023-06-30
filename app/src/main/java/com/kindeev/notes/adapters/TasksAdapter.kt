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

class TasksAdapter(private val context: Context, private val mainViewModel: MainViewModel, private val onItemClick: (task: Task, long: Boolean) -> Unit) :
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

    private fun filterTasks(tasks: List<Task>?) {
        val oldTasksList = tasks?.sortedBy{ it.time }?.reversed() ?: emptyList()
        val newTasksList = ArrayList(oldTasksList)
        for (task in oldTasksList){
            if (task.done){
                newTasksList.remove(task)
                newTasksList.add(task)
            }
        }
        tasksList = newTasksList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = TasksHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.task_item, parent, false)
    )

    override fun getItemCount() = tasksList.size

    override fun onBindViewHolder(holder: TasksHolder, position: Int) {
        holder.bind(tasksList[position])
        holder.binding.taskDoneLayout.setOnClickListener {
            if (holder.binding.taskDone.isChecked) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.deselect)
                    setPositiveButton(R.string.yes) { _, _ ->
                        holder.binding.taskDone.isChecked = false
                        tasksList[position].done = false
                        mainViewModel.insertTask(tasksList[position])
                        filterTasks(tasksList)
                    }
                    setNegativeButton(R.string.no) { _, _ -> }
                    show()
                }
            } else {
                holder.binding.taskDone.isChecked = true
                tasksList[position].done = true
                mainViewModel.insertTask(tasksList[position])
                filterTasks(tasksList)
            }
        }
        holder.binding.taskTitle.setOnClickListener {
            if (!States.taskEdited) {
                onItemClick(tasksList[position], false)
            }
        }
        holder.binding.taskTitle.setOnLongClickListener {
            onItemClick(tasksList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(tasks: List<Task>? = null) {
        filterTasks(tasks)
        notifyDataSetChanged()
    }
}