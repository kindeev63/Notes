package com.kindeev.notes.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM table_notes")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM table_categories")
    fun getAllCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM table_reminders")
    fun getAllReminders(): LiveData<List<Reminder>>

    @Query("SELECT * FROM table_reminders")
    fun getAllRemindersNotLiveData(): List<Reminder>

    @Insert(Note::class)
    suspend fun insertNote(note: Note)

    @Insert(Category::class)
    suspend fun insertCategory(category: Category)

    @Insert(Reminder::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder)

    @Update(Note::class)
    suspend fun updateNote(note: Note)

    @Update(Category::class)
    suspend fun updateCategory(category: Category)

    @Update(Reminder::class)
    suspend fun updateReminder(reminder: Reminder)

    @Delete(Note::class)
    suspend fun deleteNotes(notes: List<Note>)

    @Delete(Category::class)
    suspend fun deleteCategory(category: Category)
    @Delete(Reminder::class)
    suspend fun deleteReminders(reminders: List<Reminder>)

    @Query("SELECT * FROM table_notes WHERE id = :id")
    fun getNoteById(id: Int): Note?

}