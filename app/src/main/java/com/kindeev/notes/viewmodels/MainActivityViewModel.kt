package com.kindeev.notes.viewmodels

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.forEach
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kindeev.notes.R
import com.kindeev.notes.fragments.BaseFragment
import com.kindeev.notes.fragments.NotesFragment
import com.kindeev.notes.fragments.RemindersFragment
import com.kindeev.notes.fragments.TasksFragment
import com.kindeev.notes.other.Notifications
import com.kindeev.notes.receivers.AlarmReceiver
import java.lang.Exception

class MainActivityViewModel : ViewModel() {
    private var currentFrag: BaseFragment? = null
    var topMenu: Menu? = null

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.resources.getString(R.string.reminders)
            val channelDescription =
                context.resources.getString(R.string.notifi_channel_description)
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                Notifications.CHANNEL_ID, channelName, channelImportance
            ).apply {
                description = channelDescription
            }
            val notificationManager =
                context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun bottomMenuItemSelected(bottomMenuItem: MenuItem, activity: AppCompatActivity) {
        topMenu?.forEach { topMenuItem ->
            topMenuItem.isVisible = topMenuItem.itemId != R.id.delete_item
        }
        val searchItem = topMenu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setQuery("", false)
        searchView.isIconified = true
        searchItem.collapseActionView()
        when (bottomMenuItem.itemId) {
            R.id.bottom_notes_item -> {
                setFragment(NotesFragment.newInstance(), activity)
                activity.supportActionBar?.title = activity.resources.getString(R.string.all_notes)
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }

            R.id.bottom_reminder_item -> {
                setFragment(RemindersFragment.newInstance(), activity)
                activity.supportActionBar?.title = activity.resources.getString(R.string.reminders)
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }

            R.id.bottom_task_item -> {
                setFragment(TasksFragment.newInstance(), activity)
                activity.supportActionBar?.title = activity.resources.getString(R.string.all_tasks)
                activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun setFragment(fragment: BaseFragment, activity: AppCompatActivity) {
        activity.supportFragmentManager.beginTransaction().apply {
            replace(R.id.placeHolder, fragment)
            commit()
        }
        currentFrag = fragment
    }

    fun requestPermission(activity: AppCompatActivity) {
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0
            )
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, context: Context) {
        when (requestCode) {
            1 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(
                        context, R.string.must_grant_notifications_permission, Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(R.string.warning)
                    builder.setMessage(R.string.warning_text)
                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(R.string.go_to_settings) { dialog, _ ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }
    }

    fun onActivityResume(activity: AppCompatActivity, bottomNavigationView: BottomNavigationView?) {
        setFragment(
            currentFrag ?: NotesFragment.newInstance(), activity
        )
        when (val fragment = currentFrag) {
            null -> {
                activity.supportActionBar?.title = activity.resources.getString(R.string.all_notes)
                bottomNavigationView?.selectedItemId = R.id.bottom_notes_item
            }

            is NotesFragment -> {
                activity.supportActionBar?.title = try {
                    fragment.viewModel.category?.name ?: activity.resources.getString(R.string.all_notes)
                } catch (e: Exception) {
                    activity.resources.getString(R.string.all_notes)
                }
                bottomNavigationView?.selectedItemId = R.id.bottom_notes_item
            }

            is TasksFragment -> {
                activity.supportActionBar?.title = try {
                    fragment.viewModel.category?.name ?: activity.resources.getString(R.string.all_tasks)
                } catch (e: Exception) {
                    activity.resources.getString(R.string.all_tasks)
                }
                bottomNavigationView?.selectedItemId = R.id.bottom_task_item
            }

            is RemindersFragment -> {
                activity.supportActionBar?.title = activity.resources.getString(R.string.reminders)
                bottomNavigationView?.selectedItemId = R.id.bottom_reminder_item
            }
        }
    }

    fun activityOnCreateOptionsMenu(menu: Menu?) {
        topMenu = menu
        topMenu?.forEach {
            it.isVisible = it.itemId != R.id.delete_item
        }


        val searchItem = topMenu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            .setHintTextColor(Color.WHITE)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                currentFrag?.search(newText ?: "")
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchView.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchView.visibility = View.GONE
                return true
            }
        })
    }

    fun activityOnOptionsItemSelected(item: MenuItem, mainAppViewModel: MainAppViewModel, context: Context) {
        if (item.itemId == R.id.delete_item) {
            topMenu?.forEach {
                it.isVisible = it.itemId != R.id.delete_item
            }
        }
        val searchItem = topMenu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setQuery("", false)
        searchView.isIconified = true
        searchItem.collapseActionView()
        when (item.itemId) {
            android.R.id.home -> {
                val drawer = when (val fragment = currentFrag) {
                    is NotesFragment -> fragment.binding.drawerNotes
                    is TasksFragment -> fragment.binding.drawerTasks
                    else -> null
                }
                if (drawer?.isOpen == true) {
                    drawer.closeDrawer(GravityCompat.START)
                } else {
                    drawer?.openDrawer(GravityCompat.START)
                }

            }

            R.id.delete_item -> {
                when (val fragment = currentFrag) {
                    is NotesFragment -> {
                        fragment.viewModel.selectedNotes.value?.let { selectedNotes ->
                            val remindersForDelete =
                                mainAppViewModel.allReminders.value?.filter { reminder ->
                                    selectedNotes.any { note ->
                                        reminder.noteId == note.id
                                    }
                                }
                            remindersForDelete?.let { remindersList ->
                                remindersList.map { it.id }.forEach { reminderId ->
                                    cancelAlarm(reminderId, context)
                                }
                                mainAppViewModel.deleteReminders(remindersList)
                            }
                            mainAppViewModel.deleteNotes(selectedNotes)
                            fragment.viewModel.clearSelectedNotes()
                        }

                    }

                    is RemindersFragment -> {
                        fragment.viewModel.selectedReminders.value?.let { selectedReminders ->
                            selectedReminders.map { it.id }.forEach { reminderId ->
                                cancelAlarm(reminderId, context)
                            }
                            mainAppViewModel.deleteReminders(selectedReminders)
                            fragment.viewModel.clearSelectedReminders()
                        }
                    }
                }
            }
        }
    }

    private fun cancelAlarm(reminderId: Int, context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderId, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun activityOnBackPressed(activity: AppCompatActivity) {
        when (val fragment = currentFrag) {
            is NotesFragment -> {
                if (fragment.viewModel.selectedNotes.value?.isNotEmpty() == true) {
                    fragment.viewModel.clearSelectedNotes()
                } else {
                    activity.finish()
                }
            }

            is RemindersFragment -> {
                if (fragment.viewModel.selectedReminders.value?.isNotEmpty() == true) {
                    fragment.viewModel.clearSelectedReminders()
                } else {
                    activity.finish()
                }
            }

            else -> activity.finish()
        }
        topMenu?.findItem(R.id.delete_item)?.isVisible = false
    }

    fun onFabClick() {
        currentFrag?.onClickNew()
    }
}