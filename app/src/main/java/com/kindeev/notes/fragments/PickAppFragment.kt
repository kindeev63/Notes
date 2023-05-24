package com.kindeev.notes.fragments

import android.app.Dialog
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.R
import com.kindeev.notes.adapters.PickAppsAdapter
import com.kindeev.notes.databinding.FragmentPickAppBinding
import com.kindeev.notes.databinding.FragmentPickNoteBinding
import com.kindeev.notes.databinding.FragmentReminderDialogBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.db.Reminder
import java.util.*

class PickAppFragment(private val listener: (ApplicationInfo) -> Unit) : DialogFragment() {
    private lateinit var binding: FragmentPickAppBinding
    private lateinit var adapter: PickAppsAdapter
    private lateinit var allAppsList: List<ApplicationInfo>
    private lateinit var packageManager: PackageManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding.apply {
            packageManager = requireContext().packageManager
            allAppsList = packageManager.getInstalledApplications(PackageManager.MATCH_ALL).filter {
                (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            val thisListener: (ApplicationInfo) -> Unit = {
                listener(it)
                dialog?.dismiss()
            }
            adapter = PickAppsAdapter(packageManager, thisListener).apply {
                setData(allAppsList)
            }
            rcPickApp.adapter = adapter
            rcPickApp.layoutManager = LinearLayoutManager(requireContext())

            ePickAppSearch.addTextChangedListener {
                search(it.toString())
            }
        }
        return binding.root
    }

    fun search(text: String) {
        val appsList = filterApps(allAppsList, text)
        adapter.setData(appsList)
    }

    private fun filterApps(
        apps: List<ApplicationInfo>,
        searchText: String
    ): List<ApplicationInfo> {
        return apps.filter { it.loadLabel(packageManager).contains(searchText) }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = resources.displayMetrics.widthPixels
            dialog.window?.setLayout((width / 1.2).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentPickAppBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        return dialog
    }

    companion object {
        @JvmStatic
        fun newInstance(listener: (ApplicationInfo) -> Unit) = PickAppFragment(listener)
    }
}