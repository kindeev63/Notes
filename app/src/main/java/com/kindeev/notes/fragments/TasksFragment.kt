package com.kindeev.notes.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.CategoriesAdapter
import com.kindeev.notes.adapters.TasksAdapter
import com.kindeev.notes.databinding.FragmentTasksBinding
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.NoteViewModel
import java.util.*

class TasksFragment : BaseFragment() {
    lateinit var binding: FragmentTasksBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var tasksAdapter: TasksAdapter
    private var tasksList = emptyList<Task>()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var categoriesList = emptyList<Category>()
    var currentCategoryName: String? = null
    private var searchText: String = ""
    private var color: Int = -1

    override fun onClickNew(){
        openTask()
    }
    override fun search(text: String) {
        searchText = text
        tasksList = filterTasks()
        tasksAdapter.setData(tasks = tasksList)
        binding.noTasks.visibility = if (tasksList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setCategory(categoryName: String?) {
        (activity as AppCompatActivity).supportActionBar?.title = categoryName ?: resources.getString(R.string.all_tasks)
        currentCategoryName = categoryName
        tasksList = filterTasks()
        tasksAdapter.setData(tasks = tasksList)
        binding.noTasks.visibility = if (tasksList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterTasks(): List<Task> {
        val newTasks = if (currentCategoryName == null || noteViewModel.allTasks.value == null) {
            noteViewModel.allTasks.value ?: emptyList()
        } else {
            noteViewModel.allTasks.value?.filter { currentCategoryName in it.categories.split(", ") } ?: emptyList()
        }
        val tasks = if (noteViewModel.colorFilter) newTasks.filter { it.color == color } else newTasks
        return tasks.filter { it.title.lowercase().contains(searchText.lowercase()) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()

        val onClickTask: (Task, Boolean) -> Unit =
            { task: Task, long: Boolean ->
                if (long) {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(R.string.delete_task)
                        setPositiveButton(R.string.delete) { _, _ ->
                            noteViewModel.deleteTask(task)
                        }
                        setNegativeButton(R.string.cancel) { _, _ -> }
                        show()
                    }
                } else {
                    openTask(task)
                }

            }

        tasksAdapter = TasksAdapter(requireContext(), noteViewModel, onClickTask)
        binding.apply {
            setSpinnerAdapter()
            if (noteViewModel.colorFilter){
                colorFilterTasks.visibility = View.VISIBLE
                chColorTasks.text = ""
            } else {
                colorFilterTasks.visibility = View.GONE
                chColorTasks.text = resources.getString(R.string.color)
            }
            chColorTasks.setOnClickListener {
                if (chColorTasks.isChecked){
                    colorFilterTasks.visibility = View.VISIBLE
                    chColorTasks.text = ""
                    noteViewModel.colorFilter = true
                } else {
                    colorFilterTasks.visibility = View.GONE
                    chColorTasks.text = resources.getString(R.string.color)
                    noteViewModel.colorFilter = false
                }
                tasksList = filterTasks()
                tasksAdapter.setData(tasksList)
            }

            val screenWidth = resources.displayMetrics.widthPixels
            val newWidth = screenWidth * 5 / 6
            val layoutParams = navTasks.layoutParams
            layoutParams.width = newWidth
            navTasks.layoutParams = layoutParams
            rcTasks.adapter = tasksAdapter
            rcTasks.layoutManager = LinearLayoutManager(requireContext())
            allTasksCard.setOnClickListener {
                setCategory(null)
                drawerTasks.closeDrawer(GravityCompat.START)
            }
            addCategoryTasks.setOnClickListener {
                showEditDialog(
                    resources.getString(R.string.add_category),
                    resources.getString(R.string.add),
                    resources.getString(R.string.cancel)
                ) { name ->
                    if (name !in categoriesList.map { it.name })
                        if (name.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                R.string.category_name_is_empty,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            noteViewModel.insertCategory(Category(id = 0, name = name, type = "tasks"))
                        }
                    else
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.category_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            val onClickCategory: (Category, Boolean) -> Unit =
                { category: Category, long: Boolean ->
                    if (long) {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(R.string.want_to_do)
                            setPositiveButton(R.string.edit) { _, _ ->
                                showEditDialog(
                                    resources.getString(R.string.edit_category),
                                    resources.getString(R.string.save),
                                    resources.getString(R.string.cancel),
                                    category.name
                                ) { newName ->
                                    val oldName = category.name
                                    if (newName !in categoriesList.map { it.name }) {
                                        if (newName.isEmpty()) {
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.category_name_is_empty,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            if (currentCategoryName==category.name){
                                                setCategory(newName)
                                            }
                                            category.name = newName
                                            noteViewModel.insertCategory(category)
                                            for (task in noteViewModel.allTasks.value
                                                ?: emptyList()) {
                                                val categoriesList =
                                                    ArrayList(task.categories.split(", "))
                                                if (oldName in categoriesList) {
                                                    categoriesList.remove(oldName)
                                                    categoriesList.add(newName)
                                                    task.categories =
                                                        categoriesList.joinToString(separator = ", ")
                                                    noteViewModel.insertTask(task)
                                                }
                                            }
                                        }
                                    } else if (newName != oldName) Toast.makeText(
                                        requireContext(),
                                        R.string.category_exists,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            setNegativeButton(R.string.delete) { _, _ ->
                                if (currentCategoryName==category.name){
                                    setCategory(null)
                                }
                                noteViewModel.deleteCategory(category)
                                val categoryName = category.name
                                for (task in noteViewModel.allTasks.value ?: emptyList()) {
                                    val categoriesList = ArrayList(task.categories.split(", "))
                                    if (categoryName in categoriesList) {
                                        categoriesList.remove(categoryName)
                                        task.categories =
                                            categoriesList.joinToString(separator = ", ")
                                        noteViewModel.insertTask(task)
                                    }
                                }
                            }
                            show()
                        }
                    } else {
                        setCategory(category.name)
                        drawerTasks.closeDrawer(GravityCompat.START)
                    }
                }
            categoriesAdapter = CategoriesAdapter(onClickCategory)
            rcCategoriesTasks.adapter = categoriesAdapter
            rcCategoriesTasks.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allCategoriesOfTasks.observe(requireActivity()) {
            categoriesList = filterCategories(it, searchText)
            categoriesAdapter.setData(categories = categoriesList)
        }
        noteViewModel.allTasks.observe(requireActivity()) {
            tasksList = filterTasks()
            tasksAdapter.setData(tasks = tasksList)
            binding.noTasks.visibility = if (tasksList.isEmpty()) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    private fun filterCategories(
        categoriesList: List<Category>?,
        searchText: String
    ): List<Category> {
        return categoriesList?.filter { it.name.lowercase().contains(searchText.lowercase()) } ?: emptyList()
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        categoryName: String = "",
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            val input = EditText(requireContext()).apply {
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

    private fun openTask(task: Task? = null) {
        val idsList = noteViewModel.allTasks.value?.map { it.id } ?: emptyList()
        var taskId = 0
        if (task==null){
            while (true) {
                if (taskId !in idsList) break
                taskId++
            }
        } else {
            taskId = task.id
        }
        val dialogFragment = TaskDialogFragment.newInstance(task, taskId, noteViewModel)
        dialogFragment.show(childFragmentManager, "task_dialog")
    }

    private fun setSpinnerAdapter() {
        val colorAdapter = object : ArrayAdapter<Int>(requireContext(), R.layout.spinner_item, Colors.colors) {
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
        binding.colorFilterTasks.adapter = colorAdapter
        binding.colorFilterTasks.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newColor = parent.getItemAtPosition(position) as Int
                color = newColor
                tasksList = filterTasks()
                tasksAdapter.setData(tasksList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не делаем
            }
        }
        binding.colorFilterTasks.setSelection(Colors.colors.indexOf(color))
    }

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}