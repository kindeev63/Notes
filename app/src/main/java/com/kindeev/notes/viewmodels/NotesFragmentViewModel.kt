package com.kindeev.notes.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.activities.NoteActivity
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.States
import java.util.ArrayList
import java.util.Date

class NotesFragmentViewModel : ViewModel() {
    private var _allNotes = emptyList<Note>()
    private val _notesList = MutableLiveData<List<Note>>()
    val notesList: LiveData<List<Note>> = _notesList
    private val _selectedNotes = MutableLiveData<List<Note>>()
    val selectedNotes: LiveData<List<Note>> = _selectedNotes
    var searchText = ""
        set(value) {
            field = value
            filterNotes()
        }
    var category: Category? = null
        set(value) {
            field = value
            filterNotes()
        }
    var colorFilter: Int? = null
        set(value) {
            field = value
            filterNotes()
        }

    fun setAllNotes(notes: List<Note>) {
        _allNotes = notes
        filterNotes()
    }

    fun clearSelectedNotes() {
        _selectedNotes.value = emptyList()
    }

    private fun filterNotes() {
        var newNotes = _allNotes.toList()
        category?.let { category ->
            newNotes = newNotes.filter { category.name in it.categories.split(", ") }
        }
        colorFilter?.let { color ->
            newNotes = newNotes.filter { it.color == color }
        }
        _notesList.value = newNotes.filter { it.title.lowercase().contains(searchText.lowercase()) }
            .sortedBy { it.time }.reversed()
    }

    private fun createNote(notes: List<Note>): Note {
        val idsList = notes.map { it.id }
        var noteId = 0
        while (true) {
            if (noteId !in idsList) break
            noteId++
        }
        val currentDate = Date()
        return Note(noteId, "", "", "", currentDate.time, Color.WHITE)
    }

    fun getSpinnerAdapter(context: Context, layoutInflater: LayoutInflater) =
        object : ArrayAdapter<Int>(context, R.layout.spinner_item, Colors.colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }

            override fun getDropDownView(
                position: Int, convertView: View?, parent: ViewGroup
            ): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }
        }

    fun spinnerItemSelected() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View?, position: Int, id: Long
        ) {
            colorFilter = parent.getItemAtPosition(position) as Int
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    fun openNote(note: Note? = null, mainViewModel: MainViewModel, context: Context) {
        if (note == null) {
            val newNote = createNote(_allNotes)
            mainViewModel.insertNote(newNote) {
                openNote(note = it, mainViewModel = mainViewModel, context = context)
            }
        } else {
            val intent = Intent(context, NoteActivity::class.java).apply {
                putExtra("noteId", note.id)
            }
            context.startActivity(intent)
        }
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        categoryName: String = "",
        context: Context,
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            val input = EditText(context).apply {
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

    fun onClickNote(
        mainActivity: MainActivity, mainViewModel: MainViewModel, context: Context
    ) = { note: Note, long: Boolean ->
        if (!States.noteEdited) {
            if (long) {
                Log.e("test", "selectedNotes: ${_selectedNotes.value}")
                if (selectedNotes.value?.contains(note) == true) {
                    Log.e("test", "remove")
                    _selectedNotes.value = ArrayList(_selectedNotes.value ?: emptyList()).apply {
                        remove(note)
                    }
                } else {
                    Log.e("test", "add")
                    _selectedNotes.value = ArrayList(_selectedNotes.value ?: emptyList()).apply {
                        add(note)
                    }
                }
            } else {
                if (selectedNotes.value?.isEmpty() != false) {
                    openNote(note = note, mainViewModel = mainViewModel, context = context)
                } else {
                    if (selectedNotes.value?.contains(note) == true) {
                        _selectedNotes.value =
                            ArrayList(_selectedNotes.value ?: emptyList()).apply {
                                remove(note)
                            }
                    } else {
                        _selectedNotes.value =
                            ArrayList(_selectedNotes.value ?: emptyList()).apply {
                                add(note)
                            }
                    }
                }
            }
            if (selectedNotes.value?.isNotEmpty() != true) {
                mainActivity.topMenu?.forEach {
                    it.isVisible = it.itemId != R.id.delete_item
                }

            } else {
                mainActivity.topMenu?.forEach {
                    it.isVisible = it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                }
            }
        }
    }

    fun getDrawerLayoutParams(
        context: Context, layoutParams: ViewGroup.LayoutParams
    ): ViewGroup.LayoutParams {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val newWidth = screenWidth * 5 / 6
        layoutParams.width = newWidth
        return layoutParams
    }

    fun addCategory(context: Context, mainViewModel: MainViewModel) {
        showEditDialog(
            title = context.resources.getString(R.string.add_category),
            textOk = context.resources.getString(R.string.add),
            textCancel = context.resources.getString(R.string.cancel),
            context = context
        ) { name ->
            mainViewModel.allCategoriesOfNotes.value?.let { allCategories ->
                if (name !in allCategories.map { it.name }) {
                    if (name.isEmpty()) {
                        Toast.makeText(
                            context, R.string.category_name_is_empty, Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        mainViewModel.insertCategory(
                            Category(
                                id = 0, name = name, type = "notes"
                            )
                        )
                    }
                } else {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.category_exists),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun onClickCategory(
        context: Context,
        mainViewModel: MainViewModel,
        mainActivity: MainActivity,
        afterSelectCategory: () -> Unit
    ): (Category, Boolean) -> Unit {
        return { currentCategory: Category, long: Boolean ->
            if (long) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.want_to_do)
                    setPositiveButton(R.string.edit) { _, _ ->
                        showEditDialog(
                            title = context.resources.getString(R.string.edit_category),
                            textOk = context.resources.getString(R.string.save),
                            textCancel = context.resources.getString(R.string.cancel),
                            categoryName = currentCategory.name,
                            context = context
                        ) { newName ->
                            val oldName = currentCategory.name
                            if (mainViewModel.allCategoriesOfNotes.value?.let { allCategories ->
                                    newName !in allCategories.map { it.name }
                                } == true) {
                                if (newName.isEmpty()) {
                                    Toast.makeText(
                                        context, R.string.category_name_is_empty, Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    currentCategory.name = newName
                                    mainViewModel.insertCategory(currentCategory)
                                    if (category?.name == oldName) {
                                        category = currentCategory
                                    }
                                    for (note in _allNotes) {
                                        val categoriesList = ArrayList(note.categories.split(", "))
                                        if (oldName in categoriesList) {
                                            categoriesList.remove(oldName)
                                            categoriesList.add(newName)
                                            note.categories =
                                                categoriesList.joinToString(separator = ", ")
                                            mainViewModel.insertNote(note)
                                        }
                                    }
                                }
                            } else if (newName != oldName) Toast.makeText(
                                context, R.string.category_exists, Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    setNegativeButton(R.string.delete) { _, _ ->
                        mainViewModel.deleteCategory(currentCategory)
                        val categoryName = currentCategory.name
                        for (note in _allNotes) {
                            val categoriesList = ArrayList(note.categories.split(", "))
                            if (categoryName in categoriesList) {
                                categoriesList.remove(categoryName)
                                note.categories = categoriesList.joinToString(separator = ", ")
                                mainViewModel.insertNote(note)
                            }
                        }
                        if (category == currentCategory) {
                            category = null
                            mainActivity.supportActionBar?.title =
                                context.resources.getString(R.string.all_notes)
                        }
                    }
                    show()
                }
            } else {
                category = currentCategory
                mainActivity.supportActionBar?.title = currentCategory.name
                afterSelectCategory()
            }
        }
    }
}