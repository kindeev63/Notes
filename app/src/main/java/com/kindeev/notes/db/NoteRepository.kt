package com.kindeev.notes.db

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val allCategories: LiveData<List<Category>> = noteDao.getAllCategories()
    val allReminders: LiveData<List<Reminder>> = noteDao.getAllReminders()

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun insertCategory(category: Category) {
        noteDao.insertCategory(category)
    }

    suspend fun insertReminder(reminder: Reminder) {
        noteDao.insertReminder(reminder)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun updateCategory(category: Category) {
        noteDao.updateCategory(category)
    }

    suspend fun updateReminder(reminder: Reminder) {
        noteDao.updateReminder(reminder)
    }

    suspend fun deleteNotes(notes: List<Note>) {
        noteDao.deleteNotes(notes)
    }

    suspend fun deleteCategory(category: Category) {
        noteDao.deleteCategory(category)
    }

    suspend fun deleteReminders(reminders: List<Reminder>) {
        noteDao.deleteReminders(reminders)
    }

    fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

}