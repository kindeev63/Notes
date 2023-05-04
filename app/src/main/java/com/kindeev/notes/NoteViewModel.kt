package com.kindeev.notes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kindeev.notes.db.Category
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.NoteDataBase
import com.kindeev.notes.db.NoteRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val allCategories: LiveData<List<Category>>
    var selectedNotes = arrayListOf<Note>()

    init {
        val noteDao = NoteDataBase.getDataBase(application).getDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
        allCategories = repository.allCategories
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch {
        repository.deleteNotes(notes)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

}