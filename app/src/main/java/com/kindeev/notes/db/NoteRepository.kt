package com.kindeev.notes.db

import androidx.lifecycle.LiveData

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()
    val allTasks: LiveData<List<Task>> = noteDao.getAllTasks()
    val allCategoriesOfNotes: LiveData<List<Category>> = noteDao.getAllCategoriesByType("notes")
    val allCategoriesOfTasks: LiveData<List<Category>> = noteDao.getAllCategoriesByType("tasks")
    val allReminders: LiveData<List<Reminder>> = noteDao.getAllReminders()

    suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    suspend fun insertTask(task: Task) {
        noteDao.insertTask(task)
    }

    suspend fun insertCategory(category: Category) {
        noteDao.insertCategory(category)
    }

    suspend fun insertReminder(reminder: Reminder) {
        noteDao.insertReminder(reminder)
    }

    suspend fun deleteNotes(notes: List<Note>) {
        noteDao.deleteNotes(notes)
    }

    suspend fun deleteTask(task: Task) {
        noteDao.deleteTask(task)
    }

    suspend fun deleteCategory(category: Category) {
        noteDao.deleteCategory(category)
    }

    suspend fun deleteReminders(reminders: List<Reminder>) {
        noteDao.deleteReminders(reminders)
    }

    suspend fun getNoteById(id: Int): Note? {
        return noteDao.getNoteById(id)
    }

    fun getAllReminders() = noteDao.getAllRemindersNotLiveData()

}