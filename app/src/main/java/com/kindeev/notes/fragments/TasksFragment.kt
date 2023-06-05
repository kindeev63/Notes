package com.kindeev.notes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.databinding.FragmentTasksBinding
import com.kindeev.notes.other.NoteViewModel


class TasksFragment : BaseFragment() {
    lateinit var binding: FragmentTasksBinding
    var currentCategoryName: String? = null
    private lateinit var noteViewModel: NoteViewModel
    override fun onClickNew() {

    }

    override fun search(text: String) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()



        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}