package com.kindeev.notes.fragments

import androidx.fragment.app.Fragment
import com.kindeev.notes.db.Category

abstract class BaseFragment: Fragment() {
    abstract fun onClickNew()
}