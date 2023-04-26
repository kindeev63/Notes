package com.kindeev.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.kindeev.notes.databinding.ActivityMainBinding
import com.kindeev.notes.fragments.CategoriesFragment
import com.kindeev.notes.fragments.FragmentManager
import com.kindeev.notes.fragments.NotesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отключение автоматического включения темной темы
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        noteViewModel = (application as MainApp).noteViewModel
        FragmentManager.setFragment(NotesFragment.newInstance(), this)

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
                return@setNavigationItemSelectedListener true
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.category_item -> {
                supportActionBar?.title = if (FragmentManager.currentFrag is NotesFragment) {
                    FragmentManager.setFragment(CategoriesFragment.newInstance(), this)
                    resources.getString(R.string.categories)
                } else {
                    FragmentManager.setFragment(NotesFragment.newInstance(), this)
                    resources.getString(R.string.all_notes)
                }
            }
            android.R.id.home -> binding.drawer.openDrawer(GravityCompat.START)
        }
        return true
    }

    fun getViewModel() = noteViewModel

    private fun setCategory(categoryName: String?) {
        supportActionBar?.title = categoryName ?: resources.getString(R.string.all_notes)
        if (FragmentManager.currentFrag !is NotesFragment) {
            FragmentManager.setFragment(NotesFragment.newInstance(), this)
            val notesFrag = FragmentManager.currentFrag as NotesFragment
            notesFrag.currentCategoryName = categoryName
        } else {
            val notesFrag = FragmentManager.currentFrag as NotesFragment
            notesFrag.currentCategoryName = categoryName
            notesFrag.setCategory()
        }
    }
}