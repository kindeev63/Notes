package com.kindeev.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.databinding.RcViewItemBinding
import com.kindeev.notes.db.Category

class CategoriesAdapter(private val onItemClick: (category: Category, long: Boolean) -> Unit) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesHolder>() {
    private var categoriesList = emptyList<Category>()

    class CategoriesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = RcViewItemBinding.bind(view)
        fun bind(category: Category) = with(binding) {
            tTitle.text = category.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_view_item, parent, false)
        return CategoriesHolder(view)
    }

    override fun getItemCount() = categoriesList.size

    override fun onBindViewHolder(holder: CategoriesHolder, position: Int) {
        holder.bind(categoriesList[position])
        holder.itemView.setOnClickListener {
            onItemClick(categoriesList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(categoriesList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(data: List<Category>) {
        categoriesList = data
        notifyDataSetChanged()
    }
}