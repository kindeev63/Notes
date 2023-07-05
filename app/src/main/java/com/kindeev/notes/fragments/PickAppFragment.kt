package com.kindeev.notes.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.adapters.PickAppAdapter
import com.kindeev.notes.databinding.FragmentPickAppBinding
import com.kindeev.notes.other.ApplicationData
import com.kindeev.notes.viewmodels.PickAppFragmentViewModel

class PickAppFragment(private val listener: (ApplicationData) -> Unit) : DialogFragment() {
    private val viewModel: PickAppFragmentViewModel by viewModels()
    private lateinit var binding: FragmentPickAppBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        viewModel.setAllApps(requireContext().packageManager)
        val adapter = PickAppAdapter {
            listener(it)
            dialog?.dismiss()
        }
        viewModel.appsList.observe(viewLifecycleOwner) {
            adapter.setData(it)
        }
        binding.apply {
            rcPickApp.adapter = adapter
            rcPickApp.layoutManager = LinearLayoutManager(requireContext())
            ePickAppSearch.addTextChangedListener {
                viewModel.search(it.toString())
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = resources.displayMetrics.widthPixels
            dialog.window?.setLayout((width / 1.2).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentPickAppBinding.inflate(layoutInflater)
        return viewModel.makeDialog(requireContext(), binding.root)
    }

    companion object {
        @JvmStatic
        fun newInstance(listener: (ApplicationData) -> Unit) = PickAppFragment(listener)
    }
}