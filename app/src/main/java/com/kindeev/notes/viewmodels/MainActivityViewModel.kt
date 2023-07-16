package com.kindeev.notes.viewmodels

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
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

    fun requestPermissions(activity: AppCompatActivity) {
        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1
            )
        }
        val sharedPreferences = activity.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (sharedPreferences.getBoolean("first_run", true)) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(R.string.warning)
            builder.setMessage(R.string.warning_text)
            builder.setPositiveButton(R.string.go_to_settings) { dialog, _ ->
                val editor = sharedPreferences.edit()
                editor.putBoolean("first_run", false)
                editor.apply()
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(intent)
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray, context: Context) {
        when (requestCode) {
            0 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(
                        context, R.string.must_grant_notifications_permission, Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else -> {}
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

    fun activityOnOptionsItemSelected(item: MenuItem) {
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

            R.id.delete_item -> currentFrag?.onClickDelete()
        }
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

            is TasksFragment -> {
                if (fragment.viewModel.selectedTasks.value?.isNotEmpty() == true) {
                    fragment.viewModel.clearSelectedTasks()
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