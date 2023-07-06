package com.kindeev.notes.other

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.kindeev.notes.viewmodels.MainAppViewModel

class MainApp : Application() {
    val mainAppViewModel: MainAppViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(MainAppViewModel::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        mainAppViewModel
    }
}