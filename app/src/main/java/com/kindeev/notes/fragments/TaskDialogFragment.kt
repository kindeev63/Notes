package com.kindeev.notes.fragments

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.kindeev.notes.databinding.FragmentTaskDialogBinding
import com.kindeev.notes.db.Task
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.viewmodels.TaskDialogFragmentViewModel

class TaskDialogFragment : DialogFragment() {
    private val viewModel: TaskDialogFragmentViewModel by viewModels()

    private lateinit var binding: FragmentTaskDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.task = it.getSerializable("task") as Task
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
        binding.colorPickerTask.adapter = viewModel.getSpinnerAdapter(requireContext(), layoutInflater)
        binding.colorPickerTask.onItemSelectedListener = viewModel.spinnerItemSelected()
        binding.apply {
            categoriesPickerTask.visibility =
                if (mainViewModel().allCategoriesOfTasks.value?.isEmpty() != false) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            categoriesPickerTask.setOnClickListener {
                viewModel.showCategoriesPickerDialog(mainViewModel(), requireContext())
            }
            eTaskTitle.setText(viewModel.task?.title)
            binding.colorPickerTask.setSelection(Colors.colors.indexOf(viewModel.task?.color ?: Color.WHITE))

            return viewModel.makeDialog(requireContext(), binding.root){
                viewModel.saveTask(binding.eTaskTitle.text.toString(), mainViewModel())
                it.dismiss()
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("task", viewModel.task)
    }

    private fun mainViewModel() = (requireContext().applicationContext as MainApp).mainViewModel

    companion object {
        @JvmStatic
        fun newInstance(task: Task): TaskDialogFragment {
            val fragment = TaskDialogFragment()
            val args = Bundle()
            args.putSerializable("task", task)
            fragment.arguments = args
            return fragment
        }
    }
}