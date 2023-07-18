package com.kindeev.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.ActivityNoteBinding
import com.kindeev.notes.fragments.ReminderDialogFragment
import com.kindeev.notes.other.EdittextState
import com.kindeev.notes.other.NoteState
import com.kindeev.notes.viewmodels.NoteActivityViewModel

class NoteActivity : AppCompatActivity() {
    private val viewModel: NoteActivityViewModel by viewModels()
    private lateinit var binding: ActivityNoteBinding
    private var changeState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val toolbar = findViewById<Toolbar>(R.id.noteToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarButtons()
        States.noteEdited = true
        val noteId = intent.getIntExtra("noteId", 0)
        viewModel.getNoteById(
            noteId = noteId, mainAppViewModel = mainAppViewModel()
        ) {
            changeState = true
            binding.apply {
                eNoteTitle.setText(viewModel.note?.title)
                eNoteText.setText(viewModel.note?.text)
                binding.colorPickerNote.adapter =
                    viewModel.getSpinnerAdapter(this@NoteActivity, layoutInflater)
                binding.colorPickerNote.onItemSelectedListener =
                    viewModel.spinnerItemSelected { position ->
                        viewModel.note?.colorIndex = position
                        if (!changeState) {
                            viewModel.addNoteState(
                                NoteState(
                                    title = EdittextState(
                                        text = binding.eNoteTitle.text.toString(),
                                        selectionStart = binding.eNoteTitle.selectionStart,
                                        selectionEnd = binding.eNoteTitle.selectionEnd
                                    ),
                                    text = EdittextState(
                                        text = binding.eNoteText.text.toString(),
                                        selectionStart = binding.eNoteText.selectionStart,
                                        selectionEnd = binding.eNoteText.selectionEnd
                                    ),
                                    colorIndex = position
                                )
                            )
                        }

                    }
                binding.colorPickerNote.setSelection(viewModel.note?.colorIndex ?: 0)
            }
            changeState = false
        }
    }

    private fun toolbarButtons() {

        findViewById<ImageButton>(R.id.undoButton).setOnClickListener {
            val noteState = viewModel.undoState()
            changeState = true
            binding.eNoteTitle.setText(noteState.title.text)
            binding.eNoteTitle.setSelection(noteState.title.selectionStart, noteState.title.selectionEnd)
            binding.eNoteText.setText(noteState.text.text)
            binding.eNoteText.setSelection(noteState.text.selectionStart, noteState.text.selectionEnd)
            binding.colorPickerNote.setSelection(noteState.colorIndex)
            changeState = false
        }
        findViewById<ImageButton>(R.id.redoButton).setOnClickListener {
            val noteState = viewModel.redoState()
            changeState = true
            binding.eNoteTitle.setText(noteState.title.text)
            binding.eNoteTitle.setSelection(noteState.title.selectionStart, noteState.title.selectionEnd)
            binding.eNoteText.setText(noteState.text.text)
            binding.eNoteText.setSelection(noteState.text.selectionStart, noteState.text.selectionEnd)
            binding.colorPickerNote.setSelection(noteState.colorIndex)
            changeState = false
        }

        findViewById<ImageButton>(R.id.noteBackButton).setOnClickListener {
            onBackPressed()
        }
        findViewById<ImageButton>(R.id.addReminderButton).setOnClickListener {
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
        findViewById<ImageButton>(R.id.noteCategoryButton).apply {
            visibility = if (mainAppViewModel().allCategoriesOfNotes.value?.isNotEmpty() == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
            setOnClickListener {
                viewModel.showCategoriesPickerDialog(mainAppViewModel(), this@NoteActivity)
            }
        }

        binding.eNoteTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                if (!changeState) {
                    viewModel.addNoteState(
                        NoteState(
                            title = EdittextState(
                                text = binding.eNoteTitle.text.toString(),
                                selectionStart = binding.eNoteTitle.selectionStart,
                                selectionEnd = binding.eNoteTitle.selectionEnd
                            ),
                            text = EdittextState(
                                text = binding.eNoteText.text.toString(),
                                selectionStart = binding.eNoteText.selectionStart,
                                selectionEnd = binding.eNoteText.selectionEnd
                            ),
                            colorIndex = viewModel.note?.colorIndex ?: 0
                        )
                    )
                }

            }
        })

        binding.eNoteText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int, count: Int
            ) {
                if (!changeState) {
                    viewModel.addNoteState(
                        NoteState(
                            title = EdittextState(
                                text = binding.eNoteTitle.text.toString(),
                                selectionStart = binding.eNoteTitle.selectionStart,
                                selectionEnd = binding.eNoteTitle.selectionEnd
                            ),
                            text = EdittextState(
                                text = binding.eNoteText.text.toString(),
                                selectionStart = binding.eNoteText.selectionStart,
                                selectionEnd = binding.eNoteText.selectionEnd
                            ),
                            colorIndex = viewModel.note?.colorIndex ?: 0
                        )
                    )
                }

            }
        })
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