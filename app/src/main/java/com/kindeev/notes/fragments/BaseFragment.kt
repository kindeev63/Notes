package com.kindeev.notes.fragments

import androidx.fragment.app.Fragment
abstract class BaseFragment: Fragment() {
    abstract fun itemsSelected(): Boolean
    abstract fun onClickNew()
    abstract fun search(text: String)
    abstract fun onClickDelete()
}