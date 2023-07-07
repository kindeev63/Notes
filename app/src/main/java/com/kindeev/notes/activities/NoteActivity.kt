package com.kindeev.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.ActivityNoteBinding
import com.kindeev.notes.fragments.ReminderDialogFragment
import com.kindeev.notes.viewmodels.NoteActivityViewModel

class NoteActivity : AppCompatActivity() {
    private val viewModel: NoteActivityViewModel by viewModels()
    private lateinit var binding: ActivityNoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        States.noteEdited = true
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val noteId = intent.getIntExtra("noteId", 0)
        viewModel.getNoteById(
            noteId = noteId, mainAppViewModel = mainAppViewModel()
        ) {
            binding.apply {
                eNoteTitle.setText(viewModel.note?.title)
                eNoteText.setText(viewModel.note?.text)
                binding.colorPickerNote.adapter =
                    viewModel.getSpinnerAdapter(this@NoteActivity, layoutInflater)
                binding.colorPickerNote.onItemSelectedListener = viewModel.spinnerItemSelected()
                binding.colorPickerNote.setSelection(viewModel.note?.colorIndex ?: 0)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.note_menu, menu)
        menu?.findItem(R.id.set_category_item)?.isVisible =
            (mainAppViewModel().allCategoriesOfNotes.value?.size ?: 0) > 0
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.set_category_item -> viewModel.showCategoriesPickerDialog(mainAppViewModel(), this)
            android.R.id.home -> finish()
            R.id.add_reminder_item -> {
                viewModel.saveNote(
                    mainAppViewModel = mainAppViewModel(),
                    title = binding.eNoteTitle.text.toString(),
                    text = binding.eNoteText.text.toString()
                ) { note ->
                    val dialogFragment = ReminderDialogFragment.newInstance(
                        reminder = null, noteId = note.id
                    )
                    dialogFragment.show(supportFragmentManager, "reminder_dialog_tag")
                }
            }
        }
        return true

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.saveNote(
            mainAppViewModel = mainAppViewModel(),
            title = binding.eNoteTitle.text.toString(),
            text = binding.eNoteText.text.toString()
        )
        States.noteEdited = false
    }

    private fun mainAppViewModel() = (application as MainApp).mainAppViewModel
}