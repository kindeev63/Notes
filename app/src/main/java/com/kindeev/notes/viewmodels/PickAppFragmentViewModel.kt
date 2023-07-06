package com.kindeev.notes.viewmodels

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.ApplicationData

class PickAppFragmentViewModel : ViewModel() {
    private var _allApps = emptyList<ApplicationData>()
    private val _appsList = MutableLiveData<List<ApplicationData>>(emptyList())
    val appsList: LiveData<List<ApplicationData>> = _appsList

    fun setAllApps(packageManager: PackageManager) {
        _allApps = packageManager.getInstalledApplications(PackageManager.MATCH_ALL).filter {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }.map {
            ApplicationData(
                name = it.loadLabel(packageManager).toString(),
                icon = it.loadIcon(packageManager),
                packageName = it.packageName
            )
        }
        _appsList.value = _allApps
    }

    fun search(searchText: String) {
        _appsList.value =
            _allApps.filter { it.name.lowercase().contains(searchText.lowercase()) }
    }

    fun makeDialog(context: Context, view: View) = AlertDialog.Builder(context)
        .setView(view)
        .setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.create()
}