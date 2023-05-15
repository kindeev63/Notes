package com.kindeev.notes.activities

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.kindeev.notes.databinding.ActivityMainBinding
import com.kindeev.notes.fragments.CategoriesFragment
import com.kindeev.notes.fragments.FragmentManager
import com.kindeev.notes.fragments.NotesFragment
import androidx.appcompat.widget.SearchView
import androidx.core.view.forEach
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.other.Notifications
import com.kindeev.notes.R
import com.kindeev.notes.db.Reminder
import com.kindeev.notes.fragments.RemindersFragment
import com.kindeev.notes.receivers.AlarmReceiver

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        noteViewModel.allCategories.observe(this) {
            val menu = binding.navView.menu
            menu.clear()
            menu.add(R.id.group_id, 0, Menu.NONE, resources.getString(R.string.all_notes))
            val categories: SubMenu = menu.addSubMenu(resources.getString(R.string.categories))
            for (item in it) {
                categories.add(R.id.group_id, item.id, Menu.NONE, item.name)
            }
        }

        binding.apply {

            fab.setOnClickListener {
                FragmentManager.currentFrag?.onClickNew()
            }
            navView.setNavigationItemSelectedListener {
                if (it.itemId == 0) {
                    setCategory(null)
                } else {
                    setCategory(it.title.toString())
                }
                binding.drawer.closeDrawer(GravityCompat.START)
                return@setNavigationItemSelectedListener true
            }
        }


    }

    override fun onResume() {
        super.onResume()
        when (FragmentManager.currentFrag) {
            null -> {
                FragmentManager.setFragment(NotesFragment.newInstance(), this)
                supportActionBar?.title = resources.getString(R.string.all_notes)
            }
            is NotesFragment -> {
                FragmentManager.setFragment(FragmentManager.currentFrag as NotesFragment, this)
                (FragmentManager.currentFrag as NotesFragment).searchText = ""
                supportActionBar?.title =
                    (FragmentManager.currentFrag as NotesFragment).currentCategoryName
                        ?: resources.getString(R.string.all_notes)
            }
            is CategoriesFragment -> {
                FragmentManager.setFragment(FragmentManager.currentFrag as CategoriesFragment, this)
                supportActionBar?.title = resources.getString(R.string.categories)
            }

            is RemindersFragment -> {
                FragmentManager.setFragment(FragmentManager.currentFrag as RemindersFragment, this)
                supportActionBar?.title = resources.getString(R.string.reminders)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu



        when (FragmentManager.currentFrag){
            is NotesFragment -> {
                if (noteViewModel.selectedNotes.size == 0){
                    menu?.forEach {
                        it.isVisible = it.itemId != R.id.delete_item
                    }
                } else {
                    menu?.forEach {
                        it.isVisible = it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                    }
                }
            }
            is CategoriesFragment -> {}
            is RemindersFragment -> {
                if (noteViewModel.selectedReminders.size == 0){
                    menu?.forEach {
                        it.isVisible = it.itemId != R.id.delete_item
                    }
                } else {
                    menu?.forEach {
                        it.isVisible = it.itemId == R.id.delete_item || it.itemId == R.id.action_search
                    }
                }
            }
        }
        menu?.findItem(R.id.note_item)?.isVisible = FragmentManager.currentFrag !is NotesFragment
        menu?.findItem(R.id.category_item)?.isVisible = FragmentManager.currentFrag !is CategoriesFragment
        menu?.findItem(R.id.reminder_item)?.isVisible = FragmentManager.currentFrag !is RemindersFragment


        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            .setHintTextColor(resources.getColor(R.color.white))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

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

        if (item.itemId==R.id.delete_item){
            menu?.forEach {
                it.isVisible = it.itemId != R.id.delete_item
            }
        }
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setQuery("", false)
        searchView.isIconified = true
        searchItem.collapseActionView()
        when (item.itemId) {
            R.id.category_item -> {
                noteViewModel.selectedNotes.clear()
                FragmentManager.setFragment(CategoriesFragment.newInstance(), this)
                supportActionBar?.title = resources.getString(R.string.categories)
            }

            R.id.reminder_item -> {
                noteViewModel.selectedNotes.clear()
                FragmentManager.setFragment(RemindersFragment.newInstance(), this)
                supportActionBar?.title = resources.getString(R.string.reminders)
            }

            R.id.note_item -> {
                FragmentManager.setFragment(NotesFragment.newInstance(), this)
                supportActionBar?.title = resources.getString(R.string.all_notes)
            }


            android.R.id.home -> {
                binding.drawer.openDrawer(GravityCompat.START)
            }
            R.id.delete_item -> {
                when (FragmentManager.currentFrag){
                    is NotesFragment -> {
                        val notesFrag = FragmentManager.currentFrag as NotesFragment
                        val allReminders = noteViewModel.allReminders.value?: emptyList()
                        val deleteReminders = arrayListOf<Reminder>()
                        for (note in noteViewModel.selectedNotes){
                            for (reminder in allReminders){
                                if (reminder.noteId == note.id){
                                    cancelAlarm(reminder.id)
                                    deleteReminders.add(reminder)
                                }
                            }
                            noteViewModel.deleteReminders(deleteReminders)
                        }

                        noteViewModel.deleteNotes(noteViewModel.selectedNotes.toList())
                        noteViewModel.selectedNotes.clear()
                        notesFrag.notesAdapter.notifyDataSetChanged()
                    }
                    is RemindersFragment -> {
                        val notesFrag = FragmentManager.currentFrag as RemindersFragment
                        for (reminderId in noteViewModel.selectedReminders.map { it.id }){
                            cancelAlarm(reminderId)
                        }
                        noteViewModel.deleteReminders(noteViewModel.selectedReminders.toList())
                        noteViewModel.selectedReminders.clear()
                        notesFrag.remindersAdapter.notifyDataSetChanged()
                    }
                }


            }
        }
        menu?.findItem(R.id.note_item)?.isVisible = FragmentManager.currentFrag !is NotesFragment
        menu?.findItem(R.id.category_item)?.isVisible = FragmentManager.currentFrag !is CategoriesFragment
        menu?.findItem(R.id.reminder_item)?.isVisible = FragmentManager.currentFrag !is RemindersFragment
        return true
    }

    private fun cancelAlarm(reminderId: Int){
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, reminderId, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
        alarmManager.cancel(pendingIntent)
    }
    fun getViewModel() = noteViewModel

    private fun setCategory(categoryName: String?) {
        supportActionBar?.title = categoryName ?: resources.getString(R.string.all_notes)
        if (FragmentManager.currentFrag !is NotesFragment) {
            FragmentManager.setFragment(NotesFragment.newInstance(), this)
            val notesFrag = FragmentManager.currentFrag as NotesFragment
            notesFrag.currentCategoryName = categoryName
            menu?.findItem(R.id.note_item)?.isVisible = false
            menu?.findItem(R.id.category_item)?.isVisible = true
            menu?.findItem(R.id.reminder_item)?.isVisible = true
        } else {
            val notesFrag = FragmentManager.currentFrag as NotesFragment
            notesFrag.currentCategoryName = categoryName
            notesFrag.setCategory()
        }
    }

    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = resources.getString(R.string.reminders)
            val channelDescription = resources.getString(R.string.notifi_channel_description)
            val channelImportance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Notifications.CHANNEL_ID, channelName, channelImportance).apply {
                description = channelDescription
            }
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}