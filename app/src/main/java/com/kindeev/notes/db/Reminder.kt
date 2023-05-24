package com.kindeev.notes.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "table_reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,
    @ColumnInfo(name = "title")
    var title: String,
    @ColumnInfo(name = "description")
    var description: String,
    @ColumnInfo(name = "time")
    var time: Long,
    @ColumnInfo(name = "noteId")
    var noteId: Int?,
    @ColumnInfo(name = "packageName")
    var packageName: String?
): Serializable
