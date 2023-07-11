package com.kindeev.notes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.RemindersAdapter
import com.kindeev.notes.databinding.FragmentRemindersBinding
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.viewmodels.RemindersFragmentViewModel

class RemindersFragment : BaseFragment() {
    private lateinit var binding: FragmentRemindersBinding
    val viewModel: RemindersFragmentViewModel by viewModels()
    private var remindersAdapter: RemindersAdapter? = null

    override fun onClickNew() = viewModel.createReminder(
        activity = requireActivity(),
        fragmentManager = childFragmentManager
    )

    override fun search(text: String) {
        viewModel.searchText = text
    }

    override fun onClickDelete() {
        viewModel.deleteReminders(mainAppViewModel(), requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        remindersAdapter = RemindersAdapter(
            viewModel.onClickReminder(
                mainActivity = (activity as MainActivity),
                fragmentManager = childFragmentManager
            )
        )
        viewModel.remindersList.observe(viewLifecycleOwner) {
            remindersAdapter?.setData(reminders = it)
            binding.noReminders.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.selectedReminders.observe(viewLifecycleOwner) {
            remindersAdapter?.setData(selectedReminders = it)
        }
        mainAppViewModel().allReminders.observe(viewLifecycleOwner) {
            viewModel.setAllReminders(it)
        }
        binding.apply {
            rcReminders.adapter = remindersAdapter
            rcReminders.layoutManager = LinearLayoutManager(requireContext())
        }
        return binding.root
    }

    private fun mainAppViewModel() = (requireContext().applicationContext as MainApp).mainAppViewModel

    companion object {
        @JvmStatic
        fun newInstance() = RemindersFragment()
    }
}