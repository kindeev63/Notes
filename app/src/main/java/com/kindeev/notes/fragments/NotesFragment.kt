package com.kindeev.notes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.forEach
import androidx.core.view.iterator
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Note

class NotesFragment : BaseFragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    lateinit var notesAdapter: NotesAdapter
    private var notesList = emptyList<Note>()
    var currentCategoryName: String? = null
    private var searchText: String = ""

    override fun onClickNew() = openNote()
    override fun search(text: String) {
        searchText = text
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName, searchText)
        notesAdapter.setData(notesList)
    }

    fun setCategory() {
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName, searchText)
        notesAdapter.setData(notesList)
    }

    private fun filterNotes(notes: List<Note>?, categoryName: String?, searchText: String): List<Note> {
        val newNotes = if (categoryName == null || notes == null) {
            notes ?: emptyList()
        } else {
            notes.filter { categoryName in it.categories.split(", ") }
        }
        return newNotes.filter { it.title.contains(searchText) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()
        val onClickNote: (Note, Boolean) -> Unit =
            { note: Note, open: Boolean ->
                val mainActivity = activity as MainActivity
                if (notesAdapter.selectedNotes.size == 0){
                    mainActivity.menu?.forEach {
                        it.isVisible = it.itemId!=R.id.delete_item
                    }
                    if (open) openNote(note)
                    }
                else {
                    mainActivity.menu?.forEach {
                        it.isVisible = it.itemId==R.id.delete_item
                    }
                }

            }
        notesAdapter = NotesAdapter(onClickNote)
        binding.apply {
            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allNotes.observe(requireActivity()) {
            notesList = filterNotes(it, currentCategoryName, searchText)
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