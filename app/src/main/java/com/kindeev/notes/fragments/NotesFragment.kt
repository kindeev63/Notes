package com.kindeev.notes.fragments

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
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.viewmodels.NotesFragmentViewModel

class NotesFragment : BaseFragment() {
    lateinit var binding: FragmentNotesBinding
    private lateinit var mainViewModel: MainViewModel
    val viewModel: NotesFragmentViewModel by viewModels()
    private var notesAdapter: NotesAdapter? = null
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onClickNew() =
        viewModel.openNote(mainViewModel = mainViewModel, context = requireContext())

    override fun search(text: String) {
        viewModel.searchText = text
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        mainViewModel = (activity as MainActivity).getViewModel()
        notesAdapter = NotesAdapter(
            viewModel.notesList.value ?: emptyList(), viewModel.onClickNote(
                mainActivity = activity as MainActivity,
                mainViewModel = mainViewModel,
                context = requireContext()
            )
        )
        mainViewModel.allNotes.observe(requireActivity()) {
            viewModel.setAllNotes(it)
        }
        viewModel.notesList.observe(requireActivity()) {
            notesAdapter?.setData(notes = it)
            binding.noNotes.visibility =
                if (it.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.selectedNotes.observe(requireActivity()) {
            notesAdapter?.setData(selectedNotes = it)
        }
        binding.apply {
            colorFilterNotes.adapter = viewModel.getSpinnerAdapter(requireContext(), layoutInflater)
            colorFilterNotes.onItemSelectedListener = viewModel.spinnerItemSelected()
            colorFilterNotes.setSelection(
                viewModel.colorFilter?.let { Colors.colors.indexOf(it) } ?: 0
            )
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
                    viewModel.colorFilter = Colors.colors[0]
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
                viewModel.addCategory(requireContext(), mainViewModel)
            }
            categoriesAdapter = CategoriesAdapter(
                viewModel.onClickCategory(
                    requireContext(),
                    mainViewModel,
                    activity as MainActivity
                ) {
                    drawerNotes.closeDrawer(GravityCompat.START)
                }
            )
            rcCategoriesNotes.adapter = categoriesAdapter
            rcCategoriesNotes.layoutManager = LinearLayoutManager(requireContext())
        }
        mainViewModel.allCategoriesOfNotes.observe(requireActivity()) {
            categoriesAdapter.setData(categories = it)
        }
        return binding.root
    }


    companion object {
        @JvmStatic
        fun newInstance() = NotesFragment()
    }
}