package com.kindeev.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "table_notes")
data class Note(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "text")
    var text: String,
    @ColumnInfo(name = "categories")
    var categories: String,
    @ColumnInfo(name="time")
    var time: Long,
    @ColumnInfo(name="color")
    var color: Int,
): Serializable