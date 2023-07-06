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
import com.kindeev.notes.adapters.PickNoteAdapter
import com.kindeev.notes.databinding.FragmentPickNoteBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.viewmodels.PickNoteFragmentViewModel

class PickNoteFragment(private val listener: (Note) -> Unit) : DialogFragment() {
    private val viewModel: PickNoteFragmentViewModel by viewModels()
    private lateinit var binding: FragmentPickNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        (requireContext().applicationContext as MainApp).mainAppViewModel.allNotes.observe(viewLifecycleOwner) {
            viewModel.setAllNotes(it)
        }
        val adapter = PickNoteAdapter {
            listener(it)
            dialog?.dismiss()
        }
        viewModel.notesList.observe(viewLifecycleOwner) {
            adapter.setData(it)
        }
        binding.apply {
            rcPickNote.adapter = adapter
            rcPickNote.layoutManager = LinearLayoutManager(requireContext())
            ePickNoteSearch.addTextChangedListener {
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
        binding = FragmentPickNoteBinding.inflate(layoutInflater)
        return viewModel.makeDialog(requireContext(), binding.root)
    }

    companion object {
        @JvmStatic
        fun newInstance(listener: (Note) -> Unit) = PickNoteFragment(listener)
    }
}