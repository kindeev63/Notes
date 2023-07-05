package com.kindeev.notes.viewmodels

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import com.kindeev.notes.R
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors

class TaskDialogFragmentViewModel: ViewModel() {
    var task: Task? = null
        set(value) {
            field = value
            value?.categories?.let {
                categoriesList = ArrayList(it.split(", "))
            }
        }
    var categoriesList = arrayListOf<String>()

    fun showCategoriesPickerDialog(mainViewModel: MainViewModel, context: Context) {
        val categoriesNames: Array<String> =
            (mainViewModel.allCategoriesOfTasks.value ?: emptyList()).toList().map { it.name }
                .toTypedArray()
        val checkedCategories =
            categoriesNames.map { it in categoriesList }.toBooleanArray()


        val builder = AlertDialog.Builder(context)
        val chosenCategories = ArrayList(categoriesList)
        builder.setTitle(context.resources.getString(R.string.select_categories))
        builder.setMultiChoiceItems(
            categoriesNames,
            checkedCategories
        ) { _, index, isChecked ->
            checkedCategories[index] = isChecked
            if (checkedCategories[index]) {
                if (categoriesNames[index] !in chosenCategories) chosenCategories.add(
                    categoriesNames[index]
                )
            } else chosenCategories.remove(categoriesNames[index])
        }
        builder.setPositiveButton(R.string.save) { _, _ ->
            categoriesList = chosenCategories
        }
        builder.setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
        builder.create().show()
    }

    fun makeDialog(context: Context, view: View, onPositiveButtonClickListener: (AlertDialog) -> Unit): AlertDialog {
        val dialog = AlertDialog.Builder(context).setView(view)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.create()
        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                onPositiveButtonClickListener(dialog)
            }
        }
        return dialog
    }

    fun saveTask(title: String, mainViewModel: MainViewModel) {
        task?.let {
            mainViewModel.insertTask(
                it.copy(
                    title = title,
                    categories = categoriesList.joinToString(separator = ", "),
                ))
        }

    }

    fun getSpinnerAdapter(context: Context, layoutInflater: LayoutInflater) =
        object : ArrayAdapter<Int>(context, R.layout.spinner_item, Colors.colors) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view: View =
                    convertView ?: layoutInflater.inflate(R.layout.spinner_item, parent, false)
                val colorItem = view.findViewById<LinearLayout>(R.id.colorItem)
                val color = getItem(position)
                colorItem.setBackgroundColor(color ?: Color.TRANSPARENT)
                return view
            }
        }

    fun spinnerItemSelected() =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                task?.let {
                    it.color = parent.getItemAtPosition(position) as Int
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
}