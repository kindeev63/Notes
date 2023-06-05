package com.kindeev.notes.fragments

import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.EditText
import android.widget.Toast
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

    override fun onClickNew(){

    }
    override fun search(text: String) {
        searchText = text
        tasksList = filterTasks(noteViewModel.allTasks.value, currentCategoryName, searchText)
        tasksAdapter.setData(tasks = tasksList)
        binding.noTasks.visibility = if (tasksList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setCategory(categoryName: String?) {
        (activity as AppCompatActivity).supportActionBar?.title = categoryName ?: resources.getString(R.string.all_notes)
        currentCategoryName = categoryName
        tasksList = filterTasks(noteViewModel.allTasks.value, currentCategoryName, searchText)
        tasksAdapter.setData(tasks = tasksList)
        binding.noTasks.visibility = if (tasksList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterTasks(
        tasks: List<Task>?,
        categoryName: String?,
        searchText: String
    ): List<Task> {
        val newTasks = if (categoryName == null || tasks == null) {
            tasks ?: emptyList()
        } else {
            tasks.filter { categoryName in it.categories.split(", ") }
        }
        return newTasks.filter { it.text.lowercase().contains(searchText.lowercase()) }
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

                } else {
                    openTask(task)
                }

            }

        tasksAdapter = TasksAdapter(noteViewModel, onClickTask)
        binding.apply {
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
            tasksList = filterTasks(it, currentCategoryName, searchText)
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
        if (task == null) {
            // Создание задачи
        } else {
            // Открытие задачи
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}