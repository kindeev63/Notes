package com.kindeev.notes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentTasksBinding


class TasksFragment : BaseFragment() {
    private lateinit var binding: FragmentTasksBinding
    override fun onClickNew() {

    }

    override fun search(text: String) {
        TODO("Not yet implemented")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)



        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = TasksFragment()
    }
}