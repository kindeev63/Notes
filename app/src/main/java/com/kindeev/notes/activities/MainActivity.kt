package com.kindeev.notes.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.kindeev.notes.databinding.ActivityMainBinding
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.R
import com.kindeev.notes.viewmodels.MainActivityViewModel

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val viewModel: MainActivityViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        viewModel.requestPermission(this)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        viewModel.createNotificationChannel(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_open_drawer)
        }

        binding?.apply {
            fab.setOnClickListener {
                viewModel.onFabClick()
            }
            bNav.setOnItemSelectedListener { bottomMenuItem ->
                if (bNav.selectedItemId == bottomMenuItem.itemId) return@setOnItemSelectedListener true
                viewModel.bottomMenuItemSelected(bottomMenuItem, this@MainActivity)
                return@setOnItemSelectedListener true
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(
            requestCode, grantResults, this
        )
    }

    override fun onResume() {
        super.onResume()
        viewModel.onActivityResume(this, binding?.bNav)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        viewModel.activityOnCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.activityOnOptionsItemSelected(item, mainAppViewModel(), this)
        return true
    }

    override fun onBackPressed() {
        viewModel.activityOnBackPressed(this)
    }

    private fun mainAppViewModel() = (application as MainApp).mainAppViewModel

    fun getTopMenu() = viewModel.topMenu
}