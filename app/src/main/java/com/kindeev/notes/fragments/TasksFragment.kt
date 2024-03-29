package com.kindeev.notes.fragments

import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.CategoriesAdapter
import com.kindeev.notes.adapters.TasksAdapter
import com.kindeev.notes.databinding.FragmentTasksBinding
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.other.States
import com.kindeev.notes.viewmodels.TasksFragmentViewModel

class TasksFragment : BaseFragment() {
    lateinit var binding: FragmentTasksBinding
    val viewModel: TasksFragmentViewModel by viewModels()
    private var tasksAdapter: TasksAdapter? = null
    private var categoriesAdapter: CategoriesAdapter? = null

    override fun itemsSelected() = viewModel.selectedTasks.value?.isNotEmpty() == true
    override fun onClickNew() {
        viewModel.openTask(
            task = null,
            mainAppViewModel = mainAppViewModel(),
            fragmentManager = childFragmentManager
        )
    }

    override fun search(text: String) {
        viewModel.searchText = text
    }

    override fun onClickDelete() {
        viewModel.deleteTasks(mainAppViewModel())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        States.fragmentSwitch = false
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        mainAppViewModel().allTasks.observe(viewLifecycleOwner) {
            viewModel.setAllTasks(it)
        }
        mainAppViewModel().allCategoriesOfTasks.observe(viewLifecycleOwner) {
            categoriesAdapter?.setData(categories = it)
        }
        viewModel.tasksList.observe(viewLifecycleOwner) {
            tasksAdapter?.setData(tasks = it)
            binding.noTasks.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.selectedTasks.observe(viewLifecycleOwner) {
            tasksAdapter?.setData(selectedTasks = it)
        }
        tasksAdapter = TasksAdapter(
            onTextClick = viewModel.onTaskTextClick(
                mainAppViewModel = mainAppViewModel(),
                mainActivity = (activity as MainActivity),
                fragmentManager = childFragmentManager
            ), onCheckBoxClick = viewModel.onTaskCheckBoxClick(
                context = requireContext(), mainAppViewModel = mainAppViewModel()
            )
        )
        binding.apply {
            colorFilterTasks.adapter = viewModel.getSpinnerAdapter(requireContext(), layoutInflater)
            colorFilterTasks.onItemSelectedListener = viewModel.spinnerItemSelected()
            colorFilterTasks.setSelection(viewModel.colorFilter?.let {
                Colors.colors.map { color -> color.primary }.indexOf(it)
            } ?: 0)
            if (viewModel.colorFilter != null) {
                colorFilterTasks.visibility = View.VISIBLE
                chColorTasks.text = ""
            } else {
                colorFilterTasks.visibility = View.GONE
                chColorTasks.text = resources.getString(R.string.color)
            }
            chColorTasks.setOnClickListener {
                colorFilterTasks.setSelection(0)
                if (chColorTasks.isChecked) {
                    colorFilterTasks.visibility = View.VISIBLE
                    chColorTasks.text = ""
                    viewModel.colorFilter = Colors.colors[0].primary
                } else {
                    colorFilterTasks.visibility = View.GONE
                    chColorTasks.text = resources.getString(R.string.color)
                    viewModel.colorFilter = null
                }
            }
            navTasks.layoutParams =
                viewModel.getDrawerLayoutParams(requireContext(), navTasks.layoutParams)
            rcTasks.adapter = tasksAdapter
            rcTasks.layoutManager = LinearLayoutManager(requireContext())
            allTasksCard.setOnClickListener {
                viewModel.category = null
                drawerTasks.closeDrawer(GravityCompat.START)
                (activity as MainActivity).supportActionBar?.title =
                    resources.getString(R.string.all_tasks)
            }
            addCategoryTasks.setOnClickListener {
                viewModel.addCategory(requireContext(), mainAppViewModel())
            }
            categoriesAdapter = CategoriesAdapter(viewModel.onClickCategory(
                requireContext(), mainAppViewModel(), activity as MainActivity
            ) {
                drawerTasks.closeDrawer(GravityCompat.START)
            })
            rcCategoriesTasks.adapter = categoriesAdapter
            rcCategoriesTasks.layoutManager = LinearLayoutManager(requireContext())
        }
        return binding.root
    }

    private fun mainAppViewModel() =
        (requireContext().applicationContext as MainApp).mainAppViewModel

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}