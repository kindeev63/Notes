package com.kindeev.notes.other

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.kindeev.notes.viewmodels.MainViewModel

class MainApp : Application() {
    val mainViewModel: MainViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(MainViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        mainViewModel
    }
}