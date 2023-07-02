package com.kindeev.notes.viewmodels

import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Task
import com.kindeev.notes.fragments.TaskDialogFragment
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.States
import java.util.ArrayList
import java.util.Date

class TasksFragmentViewModel : ViewModel() {
    private var _allTasks = emptyList<Task>()
    private val _tasksList = MutableLiveData<List<Task>>()
    val tasksList: LiveData<List<Task>> = _tasksList
    var searchText = ""
        set(value) {
            field = value
            filterTasks()
        }
    var category: Category? = null
        set(value) {
            field = value
            filterTasks()
        }
    var colorFilter: Int? = null
        set(value) {
            field = value
            filterTasks()
        }

    fun setAllTasks(tasks: List<Task>) {
        _allTasks = tasks
        filterTasks()
    }

    private fun filterTasks() {
        var newTasks = _allTasks.toList()
        category?.let { category ->
            newTasks = newTasks.filter { category.name in it.categories.split(", ") }
        }
        colorFilter?.let { color ->
            newTasks = newTasks.filter { it.color == color }
        }

        val oldTasks = newTasks.reversed()
        val newTasksList = ArrayList(oldTasks)
        for (task in oldTasks){
            if (task.done){
                newTasksList.remove(task)
                newTasksList.add(task)
            }
        }
        _tasksList.value = newTasksList.filter { it.title.lowercase().contains(searchText.lowercase()) }
    }

    private fun createTask(tasks: List<Task>): Task {
        val idsList = tasks.map { it.id }
        var taskId = 0
        while (true) {
            if (taskId !in idsList) break
            taskId++
        }
        return Task(taskId, "", false, "",  Color.WHITE)
    }

    fun openTask(
        task: Task? = null,
        mainViewModel: MainViewModel,
        fragmentManager: FragmentManager
    ) {
        if (task == null) {
            val newTask = createTask(_allTasks)
            mainViewModel.insertTask(newTask) {
                openTask(
                    task = it,
                    mainViewModel = mainViewModel,
                    fragmentManager = fragmentManager
                )
            }
        } else {
            val dialogFragment = TaskDialogFragment.newInstance(task, mainViewModel)
            dialogFragment.show(fragmentManager, "task_dialog")
        }

    }

    fun getSpinnerAdapter(context: Context, layoutInflater: LayoutInflater) =
        object : ArrayAdapter<Int>(context, R.layout.spinner_item, Colors.colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }
        }

    fun spinnerItemSelected() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>,
            view: View?,
            position: Int,
            id: Long
        ) {
            colorFilter = parent.getItemAtPosition(position) as Int
        }

        override fun onNothingSelected(parent: AdapterView<*>) {
            // Ничего не делаем
        }
    }

    fun getDrawerLayoutParams(
        context: Context,
        layoutParams: ViewGroup.LayoutParams
    ): ViewGroup.LayoutParams {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val newWidth = screenWidth * 5 / 6
        layoutParams.width = newWidth
        return layoutParams
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        categoryName: String = "",
        context: Context,
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            val input = EditText(context).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                setText(categoryName)
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

    fun onTaskTextClick(
        mainViewModel: MainViewModel,
        context: Context,
        fragmentManager: FragmentManager
    ): (Task, Boolean) -> Unit {
        return { task: Task, long: Boolean ->
            if (!States.taskEdited) {
                if (long) {
                    AlertDialog.Builder(context).apply {
                        setTitle(R.string.delete_task)
                        setPositiveButton(R.string.delete) { _, _ ->
                            mainViewModel.deleteTask(task)
                        }
                        setNegativeButton(R.string.cancel) { _, _ -> }
                        show()
                    }
                } else {
                    openTask(
                        task = task,
                        mainViewModel = mainViewModel,
                        fragmentManager = fragmentManager
                    )
                }
            }
        }
    }

    fun onTaskCheckBoxClick(
        context: Context,
        mainViewModel: MainViewModel
    ): (Task) -> Unit {
        return {task: Task ->
            if (task.done) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.deselect)
                    setPositiveButton(R.string.yes) { _, _ ->
                        mainViewModel.insertTask(task.copy(done = false))
                    }
                    setNegativeButton(R.string.no) { _, _ -> }
                    show()
                }
            } else {
                mainViewModel.insertTask(task.copy(done = true))
            }
        }
    }

    fun addCategory(context: Context, mainViewModel: MainViewModel) {
        showEditDialog(
            title = context.resources.getString(R.string.add_category),
            textOk = context.resources.getString(R.string.add),
            textCancel = context.resources.getString(R.string.cancel),
            context = context
        ) { name ->
            mainViewModel.allCategoriesOfTasks.value?.let { allCategories ->
                if (name !in allCategories.map { it.name })
                    if (name.isEmpty()) {
                        Toast.makeText(
                            context,
                            R.string.category_name_is_empty,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        mainViewModel.insertCategory(
                            Category(
                                id = 0,
                                name = name,
                                type = "tasks"
                            )
                        )
                    }
                else
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.category_exists),
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }
    }

    fun onClickCategory(
        context: Context,
        mainViewModel: MainViewModel,
        mainActivity: MainActivity,
        afterSelectCategory: () -> Unit
    ): (Category, Boolean) -> Unit {
        return { currentCategory: Category, long: Boolean ->
            if (long) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.want_to_do)
                    setPositiveButton(R.string.edit) { _, _ ->
                        showEditDialog(
                            title = context.resources.getString(R.string.edit_category),
                            textOk = context.resources.getString(R.string.save),
                            textCancel = context.resources.getString(R.string.cancel),
                            categoryName = currentCategory.name,
                            context = context
                        ) { newName ->
                            val oldName = currentCategory.name
                            if (
                                mainViewModel.allCategoriesOfTasks.value?.let { allCategories ->
                                    newName !in allCategories.map { it.name }
                                } == true
                            ) {
                                if (newName.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        R.string.category_name_is_empty,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    currentCategory.name = newName
                                    mainViewModel.insertCategory(currentCategory)
                                    if (category?.name == oldName) {
                                        category = currentCategory
                                    }
                                    for (task in _allTasks) {
                                        val categoriesList =
                                            ArrayList(task.categories.split(", "))
                                        if (oldName in categoriesList) {
                                            categoriesList.remove(oldName)
                                            categoriesList.add(newName)
                                            task.categories =
                                                categoriesList.joinToString(separator = ", ")
                                            mainViewModel.insertTask(task)
                                        }
                                    }
                                }
                            } else if (newName != oldName) Toast.makeText(
                                context,
                                R.string.category_exists,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    setNegativeButton(R.string.delete) { _, _ ->
                        mainViewModel.deleteCategory(currentCategory)
                        val categoryName = currentCategory.name
                        for (task in _allTasks) {
                            val categoriesList = ArrayList(task.categories.split(", "))
                            if (categoryName in categoriesList) {
                                categoriesList.remove(categoryName)
                                task.categories =
                                    categoriesList.joinToString(separator = ", ")
                                mainViewModel.insertTask(task)
                            }
                        }
                        if (category == currentCategory) {
                            category = null
                            mainActivity.supportActionBar?.title =
                                context.resources.getString(R.string.all_tasks)
                        }
                    }
                    show()
                }
            } else {
                category = currentCategory
                mainActivity.supportActionBar?.title = currentCategory.name
                afterSelectCategory()
            }
        }
    }
}