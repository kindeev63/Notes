package com.kindeev.notes.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.adapters.NotesAdapter
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class NotesFragment : BaseFragment() {
    lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    lateinit var notesAdapter: NotesAdapter
    private var notesList = emptyList<Note>()
    var currentCategoryName: String? = null
    private var searchText: String = ""
    private var fragmentVisible = false

    override fun onClickNew() = openNote()
    override fun search(text: String) {
        searchText = text
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName, searchText)
        notesAdapter.setData(notes = notesList)
        binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setCategory(categoryName: String?) {
        activity?.actionBar?.title = categoryName ?: resources.getString(R.string.all_notes)
        currentCategoryName = categoryName
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

            navNotes.setNavigationItemSelectedListener {
                Log.e("test", "click")
                if (it.itemId == 0) {
                    setCategory(null)
                } else {
                    setCategory(it.title.toString())
                }
                binding.drawerNotes.closeDrawer(GravityCompat.START)
                return@setNavigationItemSelectedListener true
            }


        }
        noteViewModel.allNotes.observe(requireActivity()) {
            notesList = filterNotes(it, currentCategoryName, searchText)
            notesAdapter.setData(notes = notesList)
            binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
        }
        noteViewModel.allCategories.observe(requireActivity()){
            try{
                val menu = (FragmentManager.currentFrag as NotesFragment).binding.navNotes.menu
                menu.clear()
                menu.add(R.id.group_id, 0, Menu.NONE, resources.getString(R.string.all_notes))
                val categories: SubMenu = menu.addSubMenu(resources.getString(R.string.categories))
                for (item in noteViewModel.allCategories.value ?: emptyList()) {
                    categories.add(R.id.group_id, item.id, Menu.NONE, item.name)
                }
            } catch (_: Exception){}

        }
        fragmentVisible = true
        return binding.root
    }

    private fun openNote(note: Note? = null) {
        if (note == null) {
            val idsList = noteViewModel.allNotes.value?.map { it.id } ?: emptyList()
            var noteId = 0
            while (true) {
                if (noteId !in idsList) break
                noteId++
            }
            val currentDate = Date()
            val formatter = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault())
            val formattedDateTime = formatter.format(currentDate)
            val newNote = Note(noteId, "", "", "", formattedDateTime, Color.WHITE)
            noteViewModel.insertNote(newNote) {
                openNote(it)
            }
        } else {
            val intent = Intent(requireContext(), NoteActivity::class.java).apply {
                putExtra("noteId", note.id)
            }
            startActivity(intent)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}