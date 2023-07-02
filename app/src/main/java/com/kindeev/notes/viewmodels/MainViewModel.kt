package com.kindeev.notes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.kindeev.notes.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

open class MainViewModel(application: Application) : AndroidViewModel(application), Serializable {
    private val repository: NoteRepository
    val allNotes: LiveData<List<Note>>
    val allTasks: LiveData<List<Task>>
    val allCategoriesOfNotes: LiveData<List<Category>>
    val allCategoriesOfTasks: LiveData<List<Category>>
    val allReminders: LiveData<List<Reminder>>
    var colorFilter = false

    init {
        val noteDao = NoteDataBase.getDataBase(application).getDao()
        repository = NoteRepository(noteDao)
        allNotes = repository.allNotes
        allTasks = repository.allTasks
        allCategoriesOfNotes = repository.allCategoriesOfNotes
        allCategoriesOfTasks = repository.allCategoriesOfTasks
        allReminders = repository.allReminders
    }
    fun insertNote(note: Note, function: (Note) -> Unit) = viewModelScope.launch {
        repository.insertNote(note)
        function(note)
    }

    fun insertNote(note: Note) = viewModelScope.launch {
        repository.insertNote(note)
    }

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun insertCategory(category: Category) = viewModelScope.launch {
        repository.insertCategory(category)
    }

    fun insertReminder(reminder: Reminder) = viewModelScope.launch {
        repository.insertReminder(reminder)
    }

    fun deleteNotes(notes: List<Note>) = viewModelScope.launch {
        repository.deleteNotes(notes)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
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

    suspend fun getAllReminders(): List<Reminder> {
        return withContext(Dispatchers.IO) {
            repository.getAllReminders()
        }
    }
}
