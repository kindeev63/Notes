package com.kindeev.notes.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.MainActivity
import com.kindeev.notes.NoteActivity
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.NotesAdapter
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Note

class NotesFragment : BaseFragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var notesAdapter: NotesAdapter
    private var notesList = emptyList<Note>()
    private var currentCategory: Category? = null

    override fun onClickNew() {
        openNote()
    }

    fun setCategory(category: Category?){
        currentCategory = category
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategory)
        notesAdapter.setData(notesList)
    }

    private fun filterNotes(notes: List<Note>?, category: Category?): List<Note>{
        if (category==null || notes==null) return notes ?: emptyList()
        return notes.filter { category.name in it.categories.split(", ") }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()
        val onClickNote: (Note, Boolean) -> Unit =
            { note: Note, long: Boolean ->
                if (long) noteViewModel.deleteNote(note)
                else openNote(note)
            }

        notesAdapter = NotesAdapter(onClickNote)
        binding.apply {

            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
        }

        noteViewModel.allNotes.observe(requireActivity()){
            notesList = filterNotes(it, currentCategory)
            notesAdapter.setData(notesList)
        }

        return binding.root
    }

    private fun openNote(note: Note? = null) {
        val intent = Intent(requireContext(), NoteActivity::class.java)
        if (note != null) intent.putExtra("note", note)
        startActivity(intent)
    }

    companion object {

        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}