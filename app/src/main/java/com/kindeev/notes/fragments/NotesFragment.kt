package com.kindeev.notes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Note

class NotesFragment : BaseFragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var notesAdapter: NotesAdapter
    private var notesList = emptyList<Note>()
    var currentCategoryName: String? = null

    override fun onClickNew() = openNote()

    fun setCategory() {
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName)
        notesAdapter.setData(notesList)
    }

    private fun filterNotes(notes: List<Note>?, categoryName: String?): List<Note> {
        if (categoryName == null || notes == null) return notes ?: emptyList()
        return notes.filter { categoryName in it.categories.split(", ") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()
        val onClickNote: (Note, Boolean) -> Unit =
            { note: Note, long: Boolean ->
                if (long) {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle(R.string.delete_note)
                        setPositiveButton(R.string.delete) { _, _ -> noteViewModel.deleteNote(note) }
                        setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                        show()
                    }

                } else openNote(note)
            }
        notesAdapter = NotesAdapter(onClickNote)
        binding.apply {
            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allNotes.observe(requireActivity()) {
            notesList = filterNotes(it, currentCategoryName)
            notesAdapter.setData(notesList)
        }
        return binding.root
    }
    private fun openNote(note: Note? = null) {
        val intent = Intent(requireContext(), NoteActivity::class.java).apply {
            if (note != null) putExtra("note", note)
        }
        startActivity(intent)
    }
    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}