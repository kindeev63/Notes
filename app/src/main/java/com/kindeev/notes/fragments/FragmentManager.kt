package com.kindeev.notes.fragments

import androidx.appcompat.app.AppCompatActivity
import com.kindeev.notes.R

object FragmentManager {
    var currentFrag: BaseFragment? = null
    fun setFragment(newFrag: BaseFragment, activity: AppCompatActivity) {
        activity.supportFragmentManager.beginTransaction().apply {
            replace(R.id.placeHolder, newFrag)
            commit()
        }
        currentFrag = newFrag
    }
}