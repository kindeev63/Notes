package com.kindeev.notes

import android.graphics.Color
import android.graphics.PorterDuff
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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var noteViewModel: NoteViewModel
    private var menu: Menu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val color = Color.argb(255, 255, 255, 255)
        binding.fab.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN)


        supportActionBar?.title = resources.getString(R.string.all_notes)

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
                binding.drawer.closeDrawer(GravityCompat.START)
                return@setNavigationItemSelectedListener true
            }
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        this.menu = menu
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).setHintTextColor(resources.getColor(R.color.white))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                FragmentManager.currentFrag?.search(newText?: "")
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
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
        val searchItem = menu?.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        searchView.setQuery("", false)
        searchView.isIconified = true
        searchItem.collapseActionView()
        menu?.forEach {
            if (it.itemId!=R.id.action_search) it.isVisible = true
        }

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