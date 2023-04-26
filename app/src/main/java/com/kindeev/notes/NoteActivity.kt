package com.kindeev.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.kindeev.notes.databinding.ActivityNoteBinding
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Note

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private lateinit var allCategoriesList: ArrayList<Category>
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
        noteViewModel.allCategories.observe(this) {
            allCategoriesList = ArrayList(it)
        }
    }

    private fun createDialog(){
        val categoriesNames: Array<String> = allCategoriesList.map{ it.name }.toTypedArray()
        val checkedCategories = categoriesNames.map{ if (it in categoriesList) true else false}.toBooleanArray()


        val builder = AlertDialog.Builder(this)
        val chosenCategories = arrayListOf<String>()
        chosenCategories.addAll(categoriesList)
        builder.setTitle(resources.getString(R.string.select_categories))
        builder.setMultiChoiceItems(categoriesNames, checkedCategories) { dialog, which, isChecked ->
            checkedCategories[which] = isChecked
            if (checkedCategories[which]) {
                if (categoriesNames[which] !in chosenCategories) chosenCategories.add(
                    categoriesNames[which]
                )
            } else {
                chosenCategories.remove(categoriesNames[which])
            }
        }
        builder.setPositiveButton(resources.getString(R.string.save)) { dialog, which ->
            categoriesList = chosenCategories
            saveNote()
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)){ d, _ -> d.cancel() }

        val dialog = builder.create()
        dialog.show()
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
        menuInflater.inflate(R.menu.note_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.set_category_item -> {
                createDialog()
            }
            android.R.id.home -> finish()
        }
        return true

    }

    override fun onDestroy() {
        super.onDestroy()
        saveNote()
    }
    private fun saveNote(){
        val noteTitle = binding.eNoteTitle.text.toString()
        val noteText = binding.eNoteText.text.toString()
        val noteCategories = categoriesList.joinToString(separator = ", ")
        if (currentNote!=null){
            currentNote?.title = noteTitle
            currentNote?.text = noteText
            currentNote?.categories = noteCategories
            noteViewModel.updateNote(note = currentNote!!)
        }else {
            currentNote = Note(id=0, title = noteTitle, text = noteText, categories = noteCategories)
            noteViewModel.insertNote(currentNote!!)
        }
    }

}