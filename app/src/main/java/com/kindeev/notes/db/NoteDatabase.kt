package com.kindeev.notes.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Category::class, Reminder::class, Task::class], version = 1)
abstract class NoteDataBase: RoomDatabase(){
    abstract fun getDao(): NoteDao
    companion object {
        @Volatile
        private var INSTANCE: NoteDataBase? = null
        fun getDataBase(context: Context): NoteDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDataBase::class.java,
                    "notes.db"
                ).build()
                instance
            }
        }
    }
}