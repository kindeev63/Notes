package com.kindeev.notes.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.kindeev.notes.MainApp
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.States
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
    private var color: Int = -1
    private lateinit var oldNote: Note
    private val colors = listOf(
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#B22222"),
        Color.parseColor("#FF69B4"),
        Color.parseColor("#FF4500"),
        Color.parseColor("#FFD700"),
        Color.parseColor("#8B008B"),
        Color.parseColor("#8B4513"),
        Color.parseColor("#00FF00"),
        Color.parseColor("#40E0D0"),
        Color.parseColor("#696969"),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        States.noteEdited = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        noteViewModel = (application as MainApp).noteViewModel
        setData()
        setSpinnerAdapter()
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
        if (!intent.hasExtra("noteId")) return
        noteViewModel.getNoteById(intent.getIntExtra("noteId", 0)) { oldNote ->
            currentNote = oldNote.copy()
            color = oldNote.color
            binding.apply {
                eNoteTitle.setText(oldNote.title)
                eNoteText.setText(oldNote.text)
            }
            binding.colorPicker.setSelection(colors.indexOf(color))
            if (oldNote.categories.isNotEmpty()) {
                categoriesList = ArrayList(oldNote.categories.split(", "))
            }
        }
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
            currentNote?.color = color
            noteViewModel.updateNote(note = currentNote!!)
        } else {
            val newNote = Note(
                id = 0,
                title = noteTitle,
                text = noteText,
                categories = noteCategories,
                time = formattedDateTime,
                color = color
            )
            noteViewModel.insertNote(newNote)
        }
        States.noteEdited = false
    }

    private fun setSpinnerAdapter() {




        val colorAdapter = object : ArrayAdapter<Int>(this, R.layout.spinner_item, colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }
        }
        binding.colorPicker.adapter = colorAdapter
        binding.colorPicker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newColor = parent.getItemAtPosition(position) as Int
                color = newColor
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не делаем
            }
        }
    }
}