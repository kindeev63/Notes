package com.kindeev.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import com.kindeev.notes.databinding.ActivityNoteBinding
import com.kindeev.notes.db.Note
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var categoriesList: ArrayList<String> = arrayListOf()
    lateinit var noteViewModel: NoteViewModel
    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        noteViewModel = (application as MainApp).noteViewModel
        setData()
    }

    private fun createDialog() {
        val categoriesNames: Array<String> =
            (noteViewModel.allCategories.value ?: emptyList()).toList().map { it.name }
                .toTypedArray()
        val checkedCategories =
            categoriesNames.map { it in categoriesList }.toBooleanArray()


        val builder = AlertDialog.Builder(this)
        val chosenCategories = ArrayList(categoriesList)
        builder.setTitle(resources.getString(R.string.select_categories))
        builder.setMultiChoiceItems(
            categoriesNames,
            checkedCategories
        ) { _, index, isChecked ->
            checkedCategories[index] = isChecked
            if (checkedCategories[index]) {
                if (categoriesNames[index] !in chosenCategories) chosenCategories.add(
                    categoriesNames[index]
                )
            } else chosenCategories.remove(categoriesNames[index])
        }
        builder.setPositiveButton(resources.getString(R.string.save)) { _, _ ->
            categoriesList = chosenCategories
            saveNote()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { d, _ -> d.cancel() }
        builder.create().show()
    }

    private fun setData() {
        if (!intent.hasExtra("note")) return
        val note = intent.getSerializableExtra("note") as Note
        currentNote = note
        binding.apply {
            eNoteTitle.setText(note.title)
            eNoteText.setText(note.text)
        }
        if (note.categories.isEmpty()) return
        categoriesList = ArrayList(note.categories.split(", "))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if ((noteViewModel.allCategories.value?.size
                ?: 0) > 0
        ) menuInflater.inflate(R.menu.note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_category_item -> createDialog()
            android.R.id.home -> finish()
        }
        return true

    }

    override fun onDestroy() {
        super.onDestroy()
        saveNote()
    }

    private fun saveNote() {
        val currentDate = Date()
        val formatter = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault())
        val formattedDateTime = formatter.format(currentDate)
        val noteTitle = binding.eNoteTitle.text.toString()
        val noteText = binding.eNoteText.text.toString()
        val noteCategories = categoriesList.joinToString(separator = ", ")
        if (currentNote != null) {
            currentNote?.title = noteTitle
            currentNote?.text = noteText
            currentNote?.categories = noteCategories
            currentNote?.time = formattedDateTime
            noteViewModel.updateNote(note = currentNote!!)
        } else
            noteViewModel.insertNote(
                Note(
                    id = 0,
                    title = noteTitle,
                    text = noteText,
                    categories = noteCategories,
                    time = formattedDateTime
                )
            )
    }
}