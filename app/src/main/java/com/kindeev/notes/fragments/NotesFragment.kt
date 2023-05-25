package com.kindeev.notes.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.adapters.NotesAdapter
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.NoteViewModel
import java.util.*

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
        notesAdapter.setData(notes = notesList)
        binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
    }

    fun setCategory() {
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName, searchText)
        notesAdapter.setData(notes = notesList)
        binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun filterNotes(
        notes: List<Note>?,
        categoryName: String?,
        searchText: String
    ): List<Note> {
        val newNotes = if (categoryName == null || notes == null) {
            notes ?: emptyList()
        } else {
            notes.filter { categoryName in it.categories.split(", ") }
        }
        return newNotes.filter { it.title.lowercase().contains(searchText.lowercase()) }
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
                if (open) {
                    openNote(note)
                } else {
                    if (noteViewModel.selectedNotes.size == 0) {
                        val searchItem = mainActivity.menu?.findItem(R.id.action_search)
                        val searchView = searchItem?.actionView as SearchView
                        searchView.setQuery("", false)
                        searchView.isIconified = true
                        searchItem.collapseActionView()
                        mainActivity.menu?.forEach {
                            it.isVisible = it.itemId != R.id.delete_item
                        }

                    } else {
                        mainActivity.menu?.forEach {
                            it.isVisible =
                                it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                        }
                    }
                    mainActivity.menu?.findItem(R.id.note_item)?.isVisible = false
                }

            }
        notesAdapter = NotesAdapter(noteViewModel, onClickNote)
        binding.apply {
            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allNotes.observe(requireActivity()) {
            notesList = filterNotes(it, currentCategoryName, searchText)
            notesAdapter.setData(notes = notesList)
            binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    private fun openNote(note: Note? = null) {
        val intent = Intent(requireContext(), NoteActivity::class.java).apply {
            if (note != null) putExtra("noteId", note.id)
        }
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}