package com.kindeev.notes.other

import android.app.Application
import android.provider.CalendarContract.Reminders
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kindeev.notes.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

open class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val allCategories: LiveData<List<Category>>
    val allReminders: LiveData<List<Reminder>>
    var selectedNotes = arrayListOf<Note>()
    var selectedReminders = arrayListOf<Reminder>()

    init {
        val noteDao = NoteDataBase.getDataBase(application).getDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
        allCategories = repository.allCategories
        allReminders = repository.allReminders
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun insertReminder(reminder: Reminder) = viewModelScope.launch {
        repository.insertReminder(reminder)
    }

    fun updateNote(note: Note) = viewModelScope.launch {
        repository.updateNote(note)
    }

    fun updateCategory(category: Category) = viewModelScope.launch {
        repository.updateCategory(category)
    }

    fun updateReminder(reminder: Reminder) = viewModelScope.launch {
        repository.updateReminder(reminder)
    }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch {
        repository.deleteNotes(notes)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }

    fun deleteReminders(reminders: List<Reminder>) = viewModelScope.launch {
        repository.deleteReminders(reminders)
    }


    fun getNoteById(id: Int, function: (Note?) -> Unit) {
        viewModelScope.launch {
            val note = withContext(Dispatchers.IO) {
                repository.getNoteById(id)
            }
            function(note)
        }
    }

    suspend fun getAllReminders(): List<Reminder>? {
        return withContext(Dispatchers.IO) {
            repository.getAllReminders()
        }
    }
}
