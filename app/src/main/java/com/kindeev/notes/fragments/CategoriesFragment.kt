package com.kindeev.notes.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.CategoriesAdapter
import com.kindeev.notes.MainActivity
import com.kindeev.notes.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentCategoriesBinding
import com.kindeev.notes.db.Category

class CategoriesFragment : BaseFragment() {
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var categoriesList = emptyList<Category>()
    override fun onClickNew() {
        showEditDialog(resources.getString(R.string.add_category), resources.getString(R.string.add), resources.getString(R.string.cancel)) {
            noteViewModel.insertCategory(Category(id=0, name=it))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()
        val onClickCategory: (Category, Boolean) -> Unit = {
                category: Category, long: Boolean ->
            if (long) noteViewModel.deleteCategory(category)
            else showEditDialog(resources.getString(R.string.edit_category), resources.getString(R.string.save), resources.getString(R.string.cancel), category.name) {
                category.name = it
                noteViewModel.updateCategory(category)
            }
        }
        categoriesAdapter = CategoriesAdapter(onClickCategory)
        binding.apply {
            rcCategories.adapter = categoriesAdapter
            rcCategories.layoutManager = LinearLayoutManager(requireContext())
        }

        noteViewModel.allCategories.observe(requireActivity()){
            categoriesList = it
            categoriesAdapter.setData(categoriesList)
        }

        return binding.root
    }
    fun showEditDialog(title: String, textOk: String, textCancel: String, categoryName: String = "", result: (String) -> Unit){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(title)
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
        input.setText(categoryName)
        dialog.setView(input)
        dialog.setPositiveButton(textOk) {_,_->
            val text = input.text.toString()
            result(text)
        }
        dialog.setNegativeButton(textCancel) { d, _ -> d.cancel() }
        dialog.show()
    }

    companion object {

        @JvmStatic
        fun newInstance() = CategoriesFragment()
    }
}