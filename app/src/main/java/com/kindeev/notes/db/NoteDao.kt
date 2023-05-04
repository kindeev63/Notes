package com.kindeev.notes.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {
    @Query("SELECT * FROM table_notes")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM table_categories")
    fun getAllCategories(): LiveData<List<Category>>

    @Insert(Note::class)
    suspend fun insertNote(note: Note)

    @Insert(Category::class)
    suspend fun insertCategory(category: Category)

    @Update(Note::class)
    suspend fun updateNote(note: Note)

    @Update(Category::class)
    suspend fun updateCategory(category: Category)

    @Delete(Note::class)
    suspend fun deleteNotes(notes: List<Note>)

    @Delete(Category::class)
    suspend fun deleteCategory(category: Category)

}