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
import com.kindeev.notes.viewmodels.TasksFragmentViewModel

class TasksFragment : BaseFragment() {
    lateinit var binding: FragmentTasksBinding
    val viewModel: TasksFragmentViewModel by viewModels()
    private var tasksAdapter: TasksAdapter? = null
    private var categoriesAdapter: CategoriesAdapter? = null

    override fun onClickNew() {
        viewModel.openTask(
            task = null, mainViewModel = mainViewModel(), fragmentManager = childFragmentManager
        )
    }

    override fun search(text: String) {
        viewModel.searchText = text
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        mainViewModel().allTasks.observe(viewLifecycleOwner) {
            viewModel.setAllTasks(it)
        }
        mainViewModel().allCategoriesOfTasks.observe(viewLifecycleOwner) {
            categoriesAdapter?.setData(categories = it)
        }
        viewModel.tasksList.observe(viewLifecycleOwner) {
            tasksAdapter?.setData(tasks = it)
            binding.noTasks.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        tasksAdapter = TasksAdapter(
            onTextClick = viewModel.onTaskTextClick(
                mainViewModel = mainViewModel(),
                context = requireContext(),
                fragmentManager = childFragmentManager
            ), onCheckBoxClick = viewModel.onTaskCheckBoxClick(
                context = requireContext(), mainViewModel = mainViewModel()
            )
        )
        binding.apply {
            colorFilterTasks.adapter = viewModel.getSpinnerAdapter(requireContext(), layoutInflater)
            colorFilterTasks.onItemSelectedListener = viewModel.spinnerItemSelected()
            colorFilterTasks.setSelection(viewModel.colorFilter?.let { Colors.colors.indexOf(it) }
                ?: 0)
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
                    viewModel.colorFilter = Colors.colors[0]
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
                viewModel.addCategory(requireContext(), mainViewModel())
            }
            categoriesAdapter = CategoriesAdapter(viewModel.onClickCategory(
                requireContext(), mainViewModel(), activity as MainActivity
            ) {
                drawerTasks.closeDrawer(GravityCompat.START)
            })
            rcCategoriesTasks.adapter = categoriesAdapter
            rcCategoriesTasks.layoutManager = LinearLayoutManager(requireContext())
        }
        return binding.root
    }

    private fun mainViewModel() = (activity as MainActivity).getViewModel()

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}