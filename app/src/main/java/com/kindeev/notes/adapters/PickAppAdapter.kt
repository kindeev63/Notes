package com.kindeev.notes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.AppItemBinding
import com.kindeev.notes.other.ApplicationData

class PickAppAdapter(private val onItemClick: (ApplicationData) -> Unit) :
    RecyclerView.Adapter<PickAppAdapter.PickAppsHolder>() {
    private var appsList = emptyList<ApplicationData>()

    class PickAppsHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = AppItemBinding.bind(view)
        fun bind(applicationData: ApplicationData) = with(binding) {
            appName.text = applicationData.name
            appIcon.setImageDrawable(applicationData.icon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PickAppsHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)
    )

    override fun getItemCount() = appsList.size

    override fun onBindViewHolder(holder: PickAppsHolder, position: Int) {
        holder.bind(appsList[position])
        holder.itemView.setOnClickListener {
            Log.e("test", "click")
            onItemClick(appsList[position])
        }
    }
    fun setData(data: List<ApplicationData>) {
        appsList = data
        notifyDataSetChanged()
    }
}