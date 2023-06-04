package com.kindeev.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "table_tasks")
data class Task(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "done")
    var done: Boolean,
    @ColumnInfo(name = "categories")
    var categories: String,
    @ColumnInfo(name="time")
    var time: String,
    @ColumnInfo(name="color")
    var color: Int,
)
