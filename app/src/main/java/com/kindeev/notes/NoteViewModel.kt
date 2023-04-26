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

open class NoteViewModel(application: Application): AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val allCategories: LiveData<List<Category>>

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

    fun deleteNote(note: Note) = viewModelScope.launch {
        repository.deleteNote(note)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun getNoteById(id: Int): Note {
        return runBlocking {
            repository.getNoteById(id)
        }
    }

    fun getNotesByCategory(category: Int): List<Note> {
        return runBlocking {
            repository.getNotesByCategory(category)
        }
    }
}