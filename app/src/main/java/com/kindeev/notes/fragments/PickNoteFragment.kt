package com.kindeev.notes.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.R
import com.kindeev.notes.adapters.PickNotesAdapter
import com.kindeev.notes.databinding.FragmentPickNoteBinding
import com.kindeev.notes.db.Note

class PickNoteFragment(private val allNotes: List<Note>, private val listener: (Note) -> Unit) : DialogFragment() {
    private lateinit var binding: FragmentPickNoteBinding
    private lateinit var adapter: PickNotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.apply {
        val thisListener: (Note) -> Unit = {
            listener(it)
            dialog?.dismiss()
        }
            adapter = PickNotesAdapter(thisListener).apply {
                setData(allNotes)
            }
            rcPickNote.adapter = adapter
            rcPickNote.layoutManager = LinearLayoutManager(requireContext())

            ePickNoteSearch.addTextChangedListener {
                search(it.toString())
            }
        }
        return binding.root
    }

    fun search(text: String) {
        val notes = filterNotes(allNotes, text)
        adapter.setData(notes)
    }

    private fun filterNotes(
        notes: List<Note>,
        searchText: String
    ): List<Note> {
        return notes.filter { it.title.contains(searchText) }
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
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {

                dialog.dismiss()
            }
        }
        return dialog
    }

    companion object {
        @JvmStatic
        fun newInstance(notes: List<Note>, listener: (Note) -> Unit) = PickNoteFragment(notes, listener)
    }
}