package com.kindeev.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.databinding.CategoryItemBinding
import com.kindeev.notes.db.Category

class CategoriesAdapterDrawer(private val onItemClick: (category: Category) -> Unit): RecyclerView.Adapter<CategoriesAdapterDrawer.CategoriesDrawerHolder>() {
    var categoriesList = emptyList<Category>()
    class CategoriesDrawerHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = CategoryItemBinding.bind(view)
        fun bind(category: Category) = with(binding) {
            tCategoryName.text = category.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesDrawerHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.category_item, parent, false)
        return CategoriesDrawerHolder(view)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }

    override fun onBindViewHolder(holder: CategoriesDrawerHolder, position: Int) {
        holder.bind(categoriesList[position])
        holder.itemView.setOnClickListener {
            onItemClick(categoriesList[position])
        }
    }

    fun setData(data: List<Category>){
        categoriesList = data
        notifyDataSetChanged()
    }
}