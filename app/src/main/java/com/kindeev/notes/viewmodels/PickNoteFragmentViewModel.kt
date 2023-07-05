package com.kindeev.notes.viewmodels

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.db.Note

class PickNoteFragmentViewModel : ViewModel() {
    private var allNotes = emptyList<Note>()
    private val _notesList = MutableLiveData(allNotes.sortedBy{ it.time }.reversed())
    val notesList: LiveData<List<Note>> = _notesList
    private var searchText = ""

    fun setAllNotes(notes: List<Note>) {
        allNotes = notes
        search(searchText)
    }

    fun search(text: String) {
        searchText = text
        _notesList.value =
            allNotes.filter { it.title.lowercase().contains(searchText.lowercase()) }.sortedBy{ it.time }.reversed()
    }

    fun makeDialog(context: Context, view: View) = AlertDialog.Builder(context)
            .setView(view)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
}