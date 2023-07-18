package com.kindeev.notes.viewmodels

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.EdittextState
import com.kindeev.notes.other.NoteState
import java.util.Calendar
import java.util.Date

class NoteActivityViewModel : ViewModel() {
    var note: Note? = null
    private var noteStates = arrayListOf<NoteState>()
    private var stateIndex = -1

    fun getNoteById(noteId: Int, mainAppViewModel: MainAppViewModel, function: () -> Unit) {
        mainAppViewModel.getNoteById(noteId) { oldNote ->
            val newNote = oldNote ?: createNote(mainAppViewModel.allNotes.value ?: emptyList())
            note = newNote
            noteStates.add(NoteState(title = EdittextState(newNote.title, 0, 0), text = EdittextState(newNote.text, 0, 0), colorIndex = newNote.colorIndex))
            function()
        }
    }

    private fun createNote(notes: List<Note>): Note {
        val idsList = notes.map { it.id }
        var noteId = 0
        while (true) {
            if (noteId !in idsList) break
            noteId++
        }
        val currentDate = Date()
        return Note(noteId, "", "", "", currentDate.time, 0)
    }

    fun getSpinnerAdapter(context: Context, layoutInflater: LayoutInflater) =
        object : ArrayAdapter<Int>(context, R.layout.spinner_item, Colors.colors.map {it.primary}) {
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

    fun spinnerItemSelected(function: (Int) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>, view: View?, position: Int, id: Long
        ) {
            function(position)
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    fun showCategoriesPickerDialog(mainAppViewModel: MainAppViewModel, context: Context) {
        val categoriesNames: Array<String> =
            (mainAppViewModel.allCategoriesOfNotes.value ?: emptyList()).toList().map { it.name }
                .toTypedArray()
        val checkedCategories = categoriesNames.map { it in (note?.categories?.split(", ") ?: emptyList()) }.toBooleanArray()


        val builder = AlertDialog.Builder(context)
        val chosenCategories = ArrayList(note?.categories?.split(", ") ?: emptyList())
        builder.setTitle(R.string.select_categories)
        builder.setMultiChoiceItems(
            categoriesNames, checkedCategories
        ) { _, index, isChecked ->
            checkedCategories[index] = isChecked
            if (checkedCategories[index]) {
                if (categoriesNames[index] !in chosenCategories) chosenCategories.add(
                    categoriesNames[index]
                )
            } else chosenCategories.remove(categoriesNames[index])
        }
        builder.setPositiveButton(R.string.save) { _, _ ->
            note?.categories = chosenCategories.joinToString(separator = ", ")
        }
        builder.setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
        builder.create().show()
    }

    fun saveNote(
        mainAppViewModel: MainAppViewModel, title: String, text: String, function: (Note) -> Unit = {}
    ) {
        note?.let { note ->
            mainAppViewModel.insertNote(
                note = note.copy(
                    title = title, text = text, time = Calendar.getInstance().timeInMillis
                ), function = function
            )
        }
    }

    fun addNoteState(noteState: NoteState) {
        if (noteState == getState()) return
        if (stateIndex!=-1) {
            val statesToDelete = noteStates.filter { state -> noteStates.indexOf(state) > (noteStates.size + stateIndex) }
            noteStates.removeAll(statesToDelete.toSet())
            stateIndex = -1
        }

        noteStates.add(noteState)
    }

    fun redoState(): NoteState {
        if (noteStates.size + stateIndex > 0) stateIndex--
        return getState()
    }

    fun undoState(): NoteState {
        if (stateIndex <- 1) stateIndex++
        return getState()
    }

    private fun getState(): NoteState {
        return noteStates[noteStates.size + stateIndex]
    }
}