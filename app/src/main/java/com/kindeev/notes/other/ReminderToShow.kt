package com.kindeev.notes.other

import android.graphics.drawable.Drawable
import com.kindeev.notes.db.Reminder

data class ReminderToShow(
    val title: String,
    val actionIcon: Drawable,
    val time: String,
    val date: String,
    val soundIcon: Drawable,
    val reminder: Reminder
)
