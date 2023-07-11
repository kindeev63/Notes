package com.kindeev.notes.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.GravityCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kindeev.notes.*
import com.kindeev.notes.activities.MainActivity
import com.kindeev.notes.adapters.CategoriesAdapter
import com.kindeev.notes.adapters.NotesAdapter
import com.kindeev.notes.databinding.FragmentNotesBinding
import com.kindeev.notes.other.Colors
import com.kindeev.notes.other.MainApp
import com.kindeev.notes.receivers.AlarmReceiver
import com.kindeev.notes.viewmodels.NotesFragmentViewModel

class NotesFragment : BaseFragment() {
    lateinit var binding: FragmentNotesBinding
    val viewModel: NotesFragmentViewModel by viewModels()
    private var notesAdapter: NotesAdapter? = null
    private var categoriesAdapter: CategoriesAdapter? = null


    override fun onClickNew() = viewModel.openNote(
        note = null, mainAppViewModel = mainAppViewModel(), context = requireContext()
    )

    override fun search(text: String) {
        viewModel.searchText = text
    }

    override fun onClickDelete() {
        viewModel.deleteNotes(mainAppViewModel(), requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        notesAdapter = NotesAdapter(
            viewModel.onClickNote(
                mainActivity = (activity as MainActivity),
                mainAppViewModel = mainAppViewModel(),
                context = requireContext()
            )
        )
        mainAppViewModel().allNotes.observe(viewLifecycleOwner) {
            viewModel.setAllNotes(it)
        }
        mainAppViewModel().allCategoriesOfNotes.observe(viewLifecycleOwner) {
            categoriesAdapter?.setData(categories = it)
        }
        viewModel.notesList.observe(viewLifecycleOwner) {
            notesAdapter?.setData(notes = it)
            binding.noNotes.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.selectedNotes.observe(viewLifecycleOwner) {
            notesAdapter?.setData(selectedNotes = it)
        }
        binding.apply {
            colorFilterNotes.adapter = viewModel.getSpinnerAdapter(requireContext(), layoutInflater)
            colorFilterNotes.onItemSelectedListener = viewModel.spinnerItemSelected()
            colorFilterNotes.setSelection(viewModel.colorFilter?.let { Colors.colors.map{ color -> color.primary}.indexOf(it) }
                ?: 0)
            if (viewModel.colorFilter != null) {
                colorFilterNotes.visibility = View.VISIBLE
                chColorNotes.text = ""
            } else {
                colorFilterNotes.visibility = View.GONE
                chColorNotes.text = resources.getString(R.string.color)
            }
            chColorNotes.setOnClickListener {
                colorFilterNotes.setSelection(0)
                if (chColorNotes.isChecked) {
                    colorFilterNotes.visibility = View.VISIBLE
                    chColorNotes.text = ""
                    viewModel.colorFilter = Colors.colors[0].primary
                } else {
                    colorFilterNotes.visibility = View.GONE
                    chColorNotes.text = resources.getString(R.string.color)
                    viewModel.colorFilter = null
                }
            }
            navNotes.layoutParams =
                viewModel.getDrawerLayoutParams(requireContext(), navNotes.layoutParams)
            rcNotes.adapter = notesAdapter
            rcNotes.layoutManager = LinearLayoutManager(requireContext())
            allNotesCard.setOnClickListener {
                viewModel.category = null
                drawerNotes.closeDrawer(GravityCompat.START)
                (activity as MainActivity).supportActionBar?.title =
                    resources.getString(R.string.all_notes)
            }
            addCategoryNotes.setOnClickListener {
                viewModel.addCategory(requireContext(), mainAppViewModel())
            }
            categoriesAdapter = CategoriesAdapter(viewModel.onClickCategory(
                requireContext(), mainAppViewModel(), activity as MainActivity
            ) {
                drawerNotes.closeDrawer(GravityCompat.START)
            })
            rcCategoriesNotes.adapter = categoriesAdapter
            rcCategoriesNotes.layoutManager = LinearLayoutManager(requireContext())
        }
        return binding.root
    }

    private fun mainAppViewModel() =
        (requireContext().applicationContext as MainApp).mainAppViewModel


    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}