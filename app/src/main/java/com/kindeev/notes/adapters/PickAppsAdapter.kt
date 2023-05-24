package com.kindeev.notes.adapters

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.AppItemBinding

class PickAppsAdapter(private val packageManager: PackageManager, private val onItemClick: (applicationInfo: ApplicationInfo) -> Unit) :
    RecyclerView.Adapter<PickAppsAdapter.PickAppsHolder>() {
    private var appsList = emptyList<ApplicationInfo>()

    class PickAppsHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = AppItemBinding.bind(view)
        fun bind(applicationInfo: ApplicationInfo, packageManager: PackageManager) = with(binding) {
            appName.text = applicationInfo.loadLabel(packageManager)
            appIcon.setImageDrawable(applicationInfo.loadIcon(packageManager))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PickAppsHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
    )

    override fun getItemCount() = appsList.size

    override fun onBindViewHolder(holder: PickAppsHolder, position: Int) {
        holder.bind(appsList[position], packageManager)
        holder.itemView.setOnClickListener {
            onItemClick(appsList[position])
        }
    }
    fun setData(apps: List<ApplicationInfo>? = null) {
        appsList = apps ?: appsList
        notifyDataSetChanged()
    }
}