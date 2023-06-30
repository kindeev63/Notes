package com.kindeev.notes.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.ActivityNoteBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.fragments.ReminderDialogFragment
import com.kindeev.notes.other.Colors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private var categoriesList: ArrayList<String> = arrayListOf()
    lateinit var mainViewModel: MainViewModel
    private lateinit var currentNote: Note
    private var save = true
    private var color: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отключение автоматического включения темной темы
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        States.noteEdited = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mainViewModel = (application as MainApp).mainViewModel
        setData()
        setSpinnerAdapter()
    }

    private fun createDialog() {
        val categoriesNames: Array<String> =
            (mainViewModel.allCategoriesOfNotes.value ?: emptyList()).toList().map { it.name }
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
        mainViewModel.getNoteById(intent.getIntExtra("noteId", 0)) { oldNote ->
            if (oldNote == null) notSaveFinish()
            currentNote = oldNote!!.copy()
            color = oldNote.color
            binding.apply {
                eNoteTitle.setText(oldNote.title)
                eNoteText.setText(oldNote.text)
            }
            binding.colorPickerNote.setSelection(Colors.colors.indexOf(color))
            if (oldNote.categories.isNotEmpty()) {
                categoriesList = ArrayList(oldNote.categories.split(", "))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_menu, menu)
        menu?.findItem(R.id.set_category_item)?.isVisible = (mainViewModel.allCategoriesOfNotes.value?.size
            ?: 0) > 0
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_category_item -> createDialog()
            android.R.id.home -> finish()
            R.id.add_reminder_item -> {
                saveNote {note ->
                    var reminderId = 0
                    GlobalScope.launch {
                        val idsList = withContext(Dispatchers.IO) {
                            mainViewModel.getAllReminders()
                        }.map { it.id }
                        while (true) {
                            if (reminderId !in idsList) break
                            reminderId++
                        }
                        val dialogFragment =
                            ReminderDialogFragment.newInstance(
                                null,
                                reminderId,
                                mainViewModel,
                                note.id
                            )
                        dialogFragment.show(supportFragmentManager, "reminder_dialog")
                    }
                }
            }
        }
        return true

    }

    private fun notSaveFinish() {
        save = false
        Toast.makeText(this, R.string.note_not_exist, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (save) saveNote()
        States.noteEdited = false
    }

    private fun saveNote(function: (Note) -> Unit = {}) {
        val noteTitle = binding.eNoteTitle.text.toString()
        val noteText = binding.eNoteText.text.toString()
        val noteCategories = categoriesList.joinToString(separator = ", ")
        currentNote.title = noteTitle
        currentNote.text = noteText
        currentNote.categories = noteCategories
        currentNote.time = Calendar.getInstance().timeInMillis
        currentNote.color = color
        mainViewModel.insertNote(note = currentNote, function)
    }

    private fun setSpinnerAdapter() {


        val colorAdapter = object : ArrayAdapter<Int>(this, R.layout.spinner_item, Colors.colors) {
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
        binding.colorPickerNote.adapter = colorAdapter
        binding.colorPickerNote.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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