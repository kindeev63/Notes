package com.kindeev.notes.fragments

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.adapters.CategoriesAdapter
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentCategoriesBinding
import com.kindeev.notes.db.Category

class CategoriesFragment : BaseFragment() {
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var categoriesAdapter: CategoriesAdapter
    private var categoriesList = emptyList<Category>()
    private var searchText: String = ""
    override fun onClickNew() = showEditDialog(
        resources.getString(R.string.add_category),
        resources.getString(R.string.add),
        resources.getString(R.string.cancel)
    ) { name ->
        if (name !in categoriesList.map { it.name })
            if (name.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    R.string.category_name_is_empty,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                noteViewModel.insertCategory(Category(id = 0, name = name))
            }
        else
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.category_exists),
                Toast.LENGTH_SHORT
            ).show()
    }

    override fun search(text: String) {
        searchText = text
        categoriesList = filterCategories(noteViewModel.allCategories.value, searchText)
        categoriesAdapter.setData(categoriesList)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        noteViewModel = (activity as MainActivity).getViewModel()
        val onClickCategory: (Category, Boolean) -> Unit = { category: Category, long: Boolean ->
            if (long) {
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(R.string.delete_category)
                    setPositiveButton(R.string.delete) { _, _ ->
                        noteViewModel.deleteCategory(category)
                        val categoryName = category.name
                        for (note in noteViewModel.allNotes.value ?: emptyList()) {
                            val categoriesList = ArrayList(note.categories.split(", "))
                            if (categoryName in categoriesList) {
                                categoriesList.remove(categoryName)
                                note.categories = categoriesList.joinToString(separator = ", ")
                                noteViewModel.insertNote(note)
                            }
                        }
                    }
                    setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                    show()
                }
            } else showEditDialog(
                resources.getString(R.string.edit_category),
                resources.getString(R.string.save),
                resources.getString(R.string.cancel),
                category.name
            ) { newName ->
                val oldName = category.name
                if (newName !in categoriesList.map { it.name }) {
                    if (newName.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            R.string.category_name_is_empty,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        category.name = newName
                        noteViewModel.insertCategory(category)
                        for (note in noteViewModel.allNotes.value ?: emptyList()) {
                            val categoriesList = ArrayList(note.categories.split(", "))
                            if (oldName in categoriesList) {
                                categoriesList.remove(oldName)
                                categoriesList.add(newName)
                                note.categories = categoriesList.joinToString(separator = ", ")
                                noteViewModel.insertNote(note)
                            }
                        }
                    }
                } else if (newName != oldName) Toast.makeText(
                    requireContext(),
                    R.string.category_exists,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        categoriesAdapter = CategoriesAdapter(onClickCategory)
        binding.apply {
            rcCategories.adapter = categoriesAdapter
            rcCategories.layoutManager = LinearLayoutManager(requireContext())
        }
        noteViewModel.allCategories.observe(requireActivity()) {
            categoriesList = filterCategories(it, searchText)
            categoriesAdapter.setData(categoriesList)
            binding.noCategories.visibility =
                if (categoriesList.isEmpty()) View.VISIBLE else View.GONE
        }
        return binding.root
    }

    private fun filterCategories(
        categoriesList: List<Category>?,
        searchText: String
    ): List<Category> {
        return categoriesList?.filter { it.name.lowercase().contains(searchText.lowercase()) } ?: emptyList()
    }

    private fun showEditDialog(
        title: String,
        textOk: String,
        textCancel: String,
        categoryName: String = "",
        result: (String) -> Unit
    ) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            val input = EditText(requireContext()).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                setText(categoryName)
            }
            setView(input)
            setPositiveButton(textOk) { _, _ ->
                val text = input.text.toString()
                result(text)
            }
            setNegativeButton(textCancel) { d, _ -> d.cancel() }
            show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CategoriesFragment()
    }
}