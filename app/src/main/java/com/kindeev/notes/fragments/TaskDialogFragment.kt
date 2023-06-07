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
import com.kindeev.notes.other.NoteViewModel
import java.text.SimpleDateFormat
import java.util.*


class TaskDialogFragment(
    val task: Task?,
    private val taskId: Int,
    private val noteViewModel: NoteViewModel
) : DialogFragment() {
    private lateinit var binding: FragmentTaskDialogBinding
    private var color: Int = -1
    private var categoriesList: ArrayList<String> = arrayListOf()
    private val colors = listOf(
        Color.parseColor("#FFFFFF"),
        Color.parseColor("#B22222"),
        Color.parseColor("#FF69B4"),
        Color.parseColor("#FF4500"),
        Color.parseColor("#FFD700"),
        Color.parseColor("#8B008B"),
        Color.parseColor("#8B4513"),
        Color.parseColor("#00FF00"),
        Color.parseColor("#40E0D0"),
        Color.parseColor("#696969"),
    )
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
            categoriesPickerTask.visibility = if (noteViewModel.allCategoriesOfTasks.value?.isEmpty() != false) {
                View.GONE
            } else {
                View.VISIBLE
            }
            categoriesPickerTask.setOnClickListener{
                createDialog()
            }
            if (task != null) {
                eTaskTitle.setText(task.title)
                if (task.categories.isNotEmpty()) {
                    categoriesList = ArrayList(task.categories.split(", "))
                }
                color = task.color
                binding.colorPickerTask.setSelection(colors.indexOf(color))
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
                        noteViewModel.insertTask(newTask)
                        dialog.dismiss()
                    }
                }
            }
            return dialog
        }
    }

    private fun createDialog() {
        val categoriesNames: Array<String> =
            (noteViewModel.allCategoriesOfTasks.value ?: emptyList()).toList().map { it.name }
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


        val colorAdapter = object : ArrayAdapter<Int>(requireContext(), R.layout.spinner_item, colors) {
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

    companion object {
        @JvmStatic
        fun newInstance(task: Task?, taskId: Int, noteViewModel: NoteViewModel) =
            TaskDialogFragment(task, taskId, noteViewModel)
    }
}