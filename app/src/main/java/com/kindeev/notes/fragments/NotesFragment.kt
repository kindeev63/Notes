package com.kindeev.notes.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.adapters.CategoriesAdapter
import com.kindeev.notes.adapters.NotesAdapter
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*

class NotesFragment : BaseFragment() {
    lateinit var binding: FragmentNotesBinding
    private lateinit var noteViewModel: NoteViewModel
    lateinit var notesAdapter: NotesAdapter
    private var notesList = emptyList<Note>()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var categoriesList = emptyList<Category>()
    var currentCategoryName: String? = null
    private var searchText: String = ""

    override fun onClickNew() = openNote()
    override fun search(text: String) {
        searchText = text
        notesList = filterNotes(noteViewModel.allNotes.value, currentCategoryName, searchText)
        notesAdapter.setData(notes = notesList)
        binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun setCategory(categoryName: String?) {
        (activity as AppCompatActivity).supportActionBar?.title = categoryName ?: resources.getString(R.string.all_notes)
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
            val screenWidth = resources.displayMetrics.widthPixels
            val newWidth = screenWidth * 5 / 6
            val layoutParams = navNotes.layoutParams
            layoutParams.width = newWidth
            navNotes.layoutParams = layoutParams
            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
            allNotesCard.setOnClickListener {
                setCategory(null)
                drawerNotes.closeDrawer(GravityCompat.START)
            }
            addCategoryView.setOnClickListener {
                showEditDialog(
                    resources.getString(R.string.add_category),
                    resources.getString(R.string.add),
                    resources.getString(R.string.cancel)
                ) { name ->
                    if (name !in categoriesList.map { it.name })
                        if (name.isEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                R.string.category_name_is_empty,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            noteViewModel.insertCategory(Category(id = 0, name = name))
                        }
                    else
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.category_exists),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            val onClickCategory: (Category, Boolean) -> Unit =
                { category: Category, long: Boolean ->
                    if (long) {
                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(R.string.want_to_do)
                            setPositiveButton(R.string.edit) { _, _ ->
                                showEditDialog(
                                    resources.getString(R.string.edit_category),
                                    resources.getString(R.string.save),
                                    resources.getString(R.string.cancel),
                                    category.name
                                ) { newName ->
                                    val oldName = category.name
                                    if (newName !in categoriesList.map { it.name }) {
                                        if (newName.isEmpty()) {
                                            Toast.makeText(
                                                requireContext(),
                                                R.string.category_name_is_empty,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            if (currentCategoryName==category.name){
                                                setCategory(newName)
                                            }
                                            category.name = newName
                                            noteViewModel.insertCategory(category)
                                            for (note in noteViewModel.allNotes.value
                                                ?: emptyList()) {
                                                val categoriesList =
                                                    ArrayList(note.categories.split(", "))
                                                if (oldName in categoriesList) {
                                                    categoriesList.remove(oldName)
                                                    categoriesList.add(newName)
                                                    note.categories =
                                                        categoriesList.joinToString(separator = ", ")
                                                    noteViewModel.insertNote(note)
                                                }
                                            }
                                        }
                                    } else if (newName != oldName) Toast.makeText(
                                        requireContext(),
                                        R.string.category_exists,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            setNegativeButton(R.string.delete) { _, _ ->
                                if (currentCategoryName==category.name){
                                    setCategory(null)
                                }
                                noteViewModel.deleteCategory(category)
                                val categoryName = category.name
                                for (note in noteViewModel.allNotes.value ?: emptyList()) {
                                    val categoriesList = ArrayList(note.categories.split(", "))
                                    if (categoryName in categoriesList) {
                                        categoriesList.remove(categoryName)
                                        note.categories =
                                            categoriesList.joinToString(separator = ", ")
                                        noteViewModel.insertNote(note)
                                    }
                                }
                            }
                            show()
                        }
                    } else {
                        setCategory(category.name)
                        drawerNotes.closeDrawer(GravityCompat.START)
                    }
                }
            noteViewModel.allCategories.observe(requireActivity()) {
                try {
                    categoriesList = filterCategories(it, searchText)
                    categoriesAdapter.setData(categoriesList)
                } catch (_: Exception) {}
            }
            categoriesAdapter = CategoriesAdapter(onClickCategory)
            rcCategories.adapter = categoriesAdapter
            rcCategories.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allNotes.observe(requireActivity()) {
            notesList = filterNotes(it, currentCategoryName, searchText)
            notesAdapter.setData(notes = notesList)
            binding.noNotes.visibility = if (notesList.isEmpty()) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    private fun filterCategories(
        categoriesList: List<Category>?,
        searchText: String
    ): List<Category> {
        return categoriesList?.filter { it.name.lowercase().contains(searchText.lowercase()) } ?: emptyList()
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        categoryName: String = "",
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            val input = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                setText(categoryName)
            }
            setView(input)
            setPositiveButton(textOk) { _, _ ->
                val text = input.text.toString()
                result(text)
            }
            setNegativeButton(textCancel) { d, _ -> d.cancel() }
            show()
        }
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