package com.kindeev.notes.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kindeev.notes.databinding.FragmentRemindersBinding

class RemindersFragment : BaseFragment() {
    private lateinit var binding: FragmentRemindersBinding
    override fun onClickNew() {

    }

    override fun search(text: String) {

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemindersBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {

        @JvmStatic
        fun newInstance() = RemindersFragment()
    }
}