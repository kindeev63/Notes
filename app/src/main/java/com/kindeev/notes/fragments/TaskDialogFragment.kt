package com.kindeev.notes.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentTaskDialogBinding
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.viewmodels.TaskDialogFragmentViewModel
import java.util.*


class TaskDialogFragment : DialogFragment() {
    private val viewModel: TaskDialogFragmentViewModel by viewModels()

    private lateinit var binding: FragmentTaskDialogBinding
    private var categoriesList: ArrayList<String> = arrayListOf()
    private lateinit var task: Task
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            task = it.getSerializable("task") as Task
            mainViewModel = it.getSerializable("noteViewModel") as MainViewModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
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
        binding = FragmentTaskDialogBinding.inflate(layoutInflater)
        setSpinnerAdapter()
        binding.apply {
            categoriesPickerTask.visibility =
                if (mainViewModel.allCategoriesOfTasks.value?.isEmpty() != false) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            categoriesPickerTask.setOnClickListener {
                createDialog()
            }
            eTaskTitle.setText(task!!.title)
            if (task.categories.isNotEmpty()) {
                categoriesList = ArrayList(task.categories.split(", "))
            }
            binding.colorPickerTask.setSelection(Colors.colors.indexOf(task.color))

            val dialog = AlertDialog.Builder(requireContext()).setView(binding.root)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.setOnClickListener {
                        mainViewModel.insertTask(
                            task.copy(
                            title = binding.eTaskTitle.text.toString(),
                            categories = categoriesList.joinToString(separator = ", "),
                        ))
                        dialog.dismiss()
                }
            }
            return dialog
        }
    }

    private fun createDialog() {
        val categoriesNames: Array<String> =
            (mainViewModel.allCategoriesOfTasks.value ?: emptyList()).toList().map { it.name }
                .toTypedArray()
        val checkedCategories =
            categoriesNames.map { it in categoriesList }.toBooleanArray()


        val builder = AlertDialog.Builder(requireContext())
        val chosenCategories = ArrayList(categoriesList)
        builder.setTitle(resources.getString(R.string.select_categories))
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
        builder.setPositiveButton(resources.getString(R.string.save)) { _, _ ->
            categoriesList = chosenCategories
        }
        builder.setNegativeButton(resources.getString(R.string.cancel)) { d, _ -> d.cancel() }
        builder.create().show()
    }

    private fun setSpinnerAdapter() {
        val colorAdapter =
            object : ArrayAdapter<Int>(requireContext(), R.layout.spinner_item, Colors.colors) {
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
        binding.colorPickerTask.adapter = colorAdapter
        binding.colorPickerTask.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    task.color = parent.getItemAtPosition(position) as Int
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Ничего не делаем
                }
            }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("task", task)
        outState.putSerializable("noteViewModel", mainViewModel)
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task, mainViewModel: MainViewModel): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()
            args.putSerializable("task", task)
            args.putSerializable("noteViewModel", mainViewModel)
            fragment.arguments = args
            return fragment
        }
    }
}