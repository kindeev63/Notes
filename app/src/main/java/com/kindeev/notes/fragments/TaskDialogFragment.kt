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
import com.kindeev.notes.R
import com.kindeev.notes.databinding.FragmentTaskDialogBinding
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors
import com.kindeev.notes.viewmodels.MainViewModel
import java.util.*


class TaskDialogFragment() : DialogFragment() {
    private lateinit var binding: FragmentTaskDialogBinding
    private var color: Int = -1
    private var categoriesList: ArrayList<String> = arrayListOf()
    private var task: Task? = null
    private var taskId: Int = 0
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey("task")) task = it.getSerializable("task") as Task
            taskId = it.getInt("taskId", 0)
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
            categoriesPickerTask.visibility = if (mainViewModel.allCategoriesOfTasks.value?.isEmpty() != false) {
                View.GONE
            } else {
                View.VISIBLE
            }
            categoriesPickerTask.setOnClickListener{
                createDialog()
            }
            if (task != null) {
                eTaskTitle.setText(task!!.title)
                if (task!!.categories.isNotEmpty()) {
                    categoriesList = ArrayList(task!!.categories.split(", "))
                }
                color = task!!.color
                binding.colorPickerTask.setSelection(Colors.colors.indexOf(color))
            }
            val dialog = AlertDialog.Builder(requireContext()).setView(binding.root)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
            dialog.setOnShowListener {
                val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                okButton.setOnClickListener {
                    if (binding.eTaskTitle.text?.isEmpty() != false) {
                        Toast.makeText(
                            requireContext(),
                            R.string.enter_name_of_task,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        val newTask = Task(
                            taskId,
                            binding.eTaskTitle.text.toString(),
                            task?.done?: false,
                            categoriesList.joinToString(separator = ", "),
                            Calendar.getInstance().timeInMillis,
                            color
                        )
                        mainViewModel.insertTask(newTask)
                        dialog.dismiss()
                    }
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
        val colorAdapter = object : ArrayAdapter<Int>(requireContext(), R.layout.spinner_item, Colors.colors) {
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
        binding.colorPickerTask.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val newColor = parent.getItemAtPosition(position) as Int
                color = newColor
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ничего не делаем
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем состояние фрагмента
        if (task!=null) outState.putSerializable("task", task)
        outState.putInt("taskId", taskId)
        outState.putSerializable("noteViewModel", mainViewModel)
    }

    companion object {
        @JvmStatic
        fun newInstance(task: Task?, taskId: Int, mainViewModel: MainViewModel): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()
            if (task != null) args.putSerializable("task", task)
            args.putInt("taskId", taskId)
            args.putSerializable("noteViewModel", mainViewModel)
            fragment.arguments = args
            return fragment
        }
    }
}