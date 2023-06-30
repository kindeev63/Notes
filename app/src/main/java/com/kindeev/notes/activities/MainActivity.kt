package com.kindeev.notes.activities

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.kindeev.notes.databinding.ActivityMainBinding
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.other.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.fragments.*
import com.kindeev.notes.receivers.AlarmReceiver

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    var topMenu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Разрешение уже есть, выполняем код
        } else {
            // Запрашиваем разрешение у пользователя
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }

        val color = Color.argb(255, 255, 255, 255)
        binding.fab.drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)

        // Отключение автоматического включения темной темы
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        createNotificationChannel()
        noteViewModel = (application as MainApp).noteViewModel

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        }

        binding.fab.setOnClickListener {
            FragmentManager.currentFrag?.onClickNew()
        }

        binding.bNav.setOnItemSelectedListener { bottomMenuItem ->
            if (binding.bNav.selectedItemId == bottomMenuItem.itemId) return@setOnItemSelectedListener true
            noteViewModel.selectedNotes.clear()
            noteViewModel.selectedReminders.clear()
            supportActionBar?.setDisplayHomeAsUpEnabled(bottomMenuItem.itemId != R.id.bottom_reminder_item)
            noteViewModel.colorFilter = false
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
                    FragmentManager.setFragment(NotesFragment.newInstance(), this)
                    supportActionBar?.title = resources.getString(R.string.all_notes)
                }

                R.id.bottom_reminder_item -> {
                    FragmentManager.setFragment(RemindersFragment.newInstance(), this)
                    supportActionBar?.title = resources.getString(R.string.reminders)
                }

                R.id.bottom_task_item -> {
                    FragmentManager.setFragment(TasksFragment.newInstance(), this)
                    supportActionBar?.title = resources.getString(R.string.all_tasks)
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(
                        this,
                        R.string.must_grant_notifications_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.warning)
                    builder.setMessage(R.string.warning_text)
                    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.setNegativeButton(R.string.go_to_settings) { dialog, _ ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        FragmentManager.setFragment(
            FragmentManager.currentFrag ?: NotesFragment.newInstance(),
            this
        )
        when (val fragment = FragmentManager.currentFrag) {
            null -> {
                supportActionBar?.title = resources.getString(R.string.all_notes)
                binding.bNav.selectedItemId = R.id.bottom_notes_item
            }

            is NotesFragment -> {
                supportActionBar?.title =
                    fragment.currentCategoryName
                        ?: resources.getString(R.string.all_notes)
                binding.bNav.selectedItemId = R.id.bottom_notes_item
            }

            is TasksFragment -> {
                supportActionBar?.title =
                    fragment.currentCategoryName
                        ?: resources.getString(R.string.all_tasks)
                binding.bNav.selectedItemId = R.id.bottom_task_item
            }

            is RemindersFragment -> {
                supportActionBar?.title = resources.getString(R.string.reminders)
                binding.bNav.selectedItemId = R.id.bottom_reminder_item
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.topMenu = menu
        if (noteViewModel.selectedNotes.isEmpty() && noteViewModel.selectedReminders.isEmpty()) {
            menu?.forEach {
                it.isVisible = it.itemId != R.id.delete_item
            }
        } else {
            menu?.forEach {
                it.isVisible =
                    it.itemId == R.id.delete_item || it.itemId == R.id.action_search
            }
        }


        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            .setHintTextColor(resources.getColor(R.color.white))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                FragmentManager.currentFrag?.search(newText ?: "")
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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

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
                val drawer = when (val fragment = FragmentManager.currentFrag) {
                    is NotesFragment -> fragment.binding.drawerNotes
                    is TasksFragment -> fragment.binding.drawerTasks
                    else -> null
                }
                drawer?.openDrawer(GravityCompat.START)
            }

            R.id.delete_item -> {
                when (val fragment = FragmentManager.currentFrag) {
                    is NotesFragment -> {
                        val remindersForDelete =
                            noteViewModel.allReminders.value?.filter { reminder ->
                                noteViewModel.selectedNotes.any { note ->
                                    reminder.noteId == note.id
                                }
                            }
                        remindersForDelete?.let { noteViewModel.deleteReminders(it) }
                        noteViewModel.deleteNotes(noteViewModel.selectedNotes.toList())
                        noteViewModel.selectedNotes.clear()
                        fragment.notesAdapter?.notifyDataSetChanged()
                    }

                    is RemindersFragment -> {
                        noteViewModel.selectedReminders.map { it.id }.forEach { reminderId ->
                            cancelAlarm(reminderId)
                        }
                        noteViewModel.deleteReminders(noteViewModel.selectedReminders.toList())
                        noteViewModel.selectedReminders.clear()
                        fragment.remindersAdapter.notifyDataSetChanged()
                    }
                }
            }
        }
        return true
    }

    override fun onBackPressed() {
        when (val fragment = FragmentManager.currentFrag) {
            is NotesFragment -> {
                if (noteViewModel.selectedNotes.isNotEmpty()) {
                    noteViewModel.selectedNotes.clear()
                    fragment.notesAdapter?.notifyDataSetChanged()
                } else {
                    super.onBackPressed()
                }
            }

            is RemindersFragment -> {
                if (noteViewModel.selectedReminders.isNotEmpty()) {
                    noteViewModel.selectedReminders.clear()
                    fragment.remindersAdapter.notifyDataSetChanged()
                } else {
                    super.onBackPressed()
                }
            }
            else -> super.onBackPressed()
        }
        topMenu?.findItem(R.id.delete_item)?.isVisible = false
    }

    private fun cancelAlarm(reminderId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminderId,
            i,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }

    fun getViewModel() = noteViewModel

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = resources.getString(R.string.reminders)
            val channelDescription = resources.getString(R.string.notifi_channel_description)
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(
                Notifications.CHANNEL_ID,
                channelName,
                channelImportance
            ).apply {
                description = channelDescription
            }
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}