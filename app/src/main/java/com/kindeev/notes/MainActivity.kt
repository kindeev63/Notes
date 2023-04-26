package com.kindeev.notes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.databinding.ActivityMainBinding
import com.kindeev.notes.db.Category
import com.kindeev.notes.fragments.CategoriesFragment
import com.kindeev.notes.fragments.FragmentManager
import com.kindeev.notes.fragments.NotesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoriesAdapter: CategoriesAdapterDrawer
    private lateinit var noteViewModel: NoteViewModel
    private var categoriesList = emptyList<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteViewModel = (application as MainApp).noteViewModel
        FragmentManager.setFragment(NotesFragment.newInstance(), this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_open_drawer)

        noteViewModel.allCategories.observe(this) {
            categoriesList = it
            categoriesAdapter.setData(categoriesList)
        }

        binding.apply {
            val onClickCategory: (Category) -> Unit = {
                setCategory(it)
                drawer.closeDrawer(GravityCompat.START)
            }
            categoriesAdapter = CategoriesAdapterDrawer(onClickCategory)
            rcSetCategory.adapter = categoriesAdapter
            rcSetCategory.layoutManager = LinearLayoutManager(this@MainActivity)

            fab.setOnClickListener {
                FragmentManager.currentFrag?.onClickNew()
            }

            tAllCategories.setOnClickListener {
                setCategory(null)
                drawer.closeDrawer(GravityCompat.START)
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.category_item -> {
                if (FragmentManager.currentFrag is NotesFragment){
                    FragmentManager.setFragment(CategoriesFragment.newInstance(), this)
                    supportActionBar?.title = resources.getString(R.string.categories)
                } else {
                    FragmentManager.setFragment(NotesFragment.newInstance(), this)
                    supportActionBar?.title = resources.getString(R.string.all_notes)
                }
            }
            android.R.id.home -> binding.drawer.openDrawer(GravityCompat.START)
        }

        return true
    }
    fun getViewModel(): NoteViewModel{
        return noteViewModel
    }
    private fun setCategory(category: Category?){
        supportActionBar?.title = category?.name ?: resources.getString(R.string.all_notes)
        if (FragmentManager.currentFrag !is NotesFragment) FragmentManager.setFragment(NotesFragment.newInstance(), this)
        val notesFrag = supportFragmentManager.findFragmentById(R.id.placeHolder) as NotesFragment
        notesFrag.setCategory(category)
    }
}