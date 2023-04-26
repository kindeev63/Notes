package com.kindeev.notes

import android.app.Application
import androidx.lifecycle.ViewModelProvider

class MainApp : Application() {
    val noteViewModel: NoteViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(NoteViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        noteViewModel
    }
}