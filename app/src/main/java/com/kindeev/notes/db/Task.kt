package com.kindeev.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kindeev.notes.other.ItemColor
import java.io.Serializable

@Entity(tableName = "table_tasks")
data class Task(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    @ColumnInfo(name = "text")
    var title: String,
    @ColumnInfo(name = "done")
    var done: Boolean,
    @ColumnInfo(name = "categories")
    var categories: String,
    @ColumnInfo(name="colorIndex")
    var colorIndex: Int,
): Serializable
