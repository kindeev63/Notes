package com.kindeev.notes.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM table_notes")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM table_tasks")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT * FROM table_categories WHERE type = :type")
    fun getAllCategoriesByType(type: String): LiveData<List<Category>>

    @Query("SELECT * FROM table_reminders")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM table_reminders")
    fun getAllRemindersNotLiveData(): List<Reminder>

    @Insert(Note::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Insert(Task::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(Category::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Insert(Reminder::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Delete(Note::class)
    suspend fun deleteNotes(notes: List<Note>)

    @Delete(Task::class)
    suspend fun deleteTask(task: Task)

    @Delete(Category::class)
    suspend fun deleteCategory(category: Category)

    @Delete(Reminder::class)
    suspend fun deleteReminders(reminders: List<Reminder>)

    @Query("SELECT * FROM table_notes WHERE id = :id")
    suspend fun getNoteById(id: Int): Note?

}