package com.kindeev.notes.viewmodels

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.fragments.ReminderDialogFragment
import com.kindeev.notes.other.States
import java.util.ArrayList

class RemindersFragmentViewModel : ViewModel() {
    private var _allReminders = emptyList<Reminder>()
    private val _remindersList = MutableLiveData<List<Reminder>>()
    val remindersList: LiveData<List<Reminder>> = _remindersList
    private val _selectedReminders = MutableLiveData<List<Reminder>>()
    val selectedReminders: LiveData<List<Reminder>> = _selectedReminders
    var searchText = ""
        set(value) {
            field = value
            filterReminders()
        }

    fun setAllReminders(reminders: List<Reminder>) {
        _allReminders = reminders
        filterReminders()
    }

    fun clearSelectedReminders() {
        _selectedReminders.value = emptyList()
    }

    private fun filterReminders() {
        _remindersList.value =
            _allReminders.filter { it.title.lowercase().contains(searchText.lowercase()) }
    }

    private fun openReminder(
        reminder: Reminder? = null, fragmentManager: FragmentManager
    ) {
        val dialogFragment = ReminderDialogFragment.newInstance(
            reminder = reminder
        )
        dialogFragment.show(fragmentManager, "reminder_dialog")
    }

    fun createReminder(
        activity: Activity, fragmentManager: FragmentManager
    ) {
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openReminder(
                fragmentManager = fragmentManager
            )
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }
    }

    fun onClickReminder(
        mainActivity: MainActivity, fragmentManager: FragmentManager
    ) = { reminder: Reminder, long: Boolean ->
        if (!States.reminderEdited) {
            if (long) {
                if (selectedReminders.value?.contains(reminder) == true) {
                    _selectedReminders.value =
                        ArrayList(_selectedReminders.value ?: emptyList()).apply {
                            remove(reminder)
                        }
                } else {
                    _selectedReminders.value =
                        ArrayList(_selectedReminders.value ?: emptyList()).apply {
                            add(reminder)
                        }
                }
            } else {
                if (selectedReminders.value?.isEmpty() != false) {
                    openReminder(
                        reminder = reminder,
                        fragmentManager = fragmentManager
                    )
                } else {
                    if (selectedReminders.value?.contains(reminder) == true) {
                        _selectedReminders.value =
                            ArrayList(_selectedReminders.value ?: emptyList()).apply {
                                remove(reminder)
                            }
                    } else {
                        _selectedReminders.value =
                            ArrayList(_selectedReminders.value ?: emptyList()).apply {
                                add(reminder)
                            }
                    }
                }
            }
            if (selectedReminders.value?.isNotEmpty() != true) {
                mainActivity.topMenu?.forEach {
                    it.isVisible = it.itemId != R.id.delete_item
                }

            } else {
                mainActivity.topMenu?.forEach {
                    it.isVisible = it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                }
            }
        }
    }
}