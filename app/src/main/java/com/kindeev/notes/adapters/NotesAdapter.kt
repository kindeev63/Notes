package com.kindeev.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.viewmodels.MainViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.NoteItemBinding
import com.kindeev.notes.db.Note
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(private val mainViewModel: MainViewModel, private val onItemClick: (note: Note, open: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    private var notesList = mainViewModel.allNotes.value ?: emptyList()
    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note, choosingNotes: Boolean, noteSelected: Boolean) = with(binding) {
            tTitle.text = note.title
            val formatter = SimpleDateFormat("dd.MM.yyyy  HH:mm", Locale.getDefault())
            val formattedDateTime = formatter.format(note.time)
            tTime.text = formattedDateTime
            noteContent.setBackgroundColor(note.color)
            chDeleteNote.visibility =
            if (choosingNotes){
                View.VISIBLE
            } else View.GONE
            chDeleteNote.isChecked = noteSelected
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.bind(notesList[position], mainViewModel.selectedNotes.size > 0, notesList[position] in mainViewModel.selectedNotes)
        holder.itemView.setOnClickListener {
            if (!States.noteEdited) {
                val open = mainViewModel.selectedNotes.size == 0
                if (mainViewModel.selectedNotes.size > 0){
                    val note = notesList[position]
                    if (note in mainViewModel.selectedNotes) mainViewModel.selectedNotes.remove(note)
                    else mainViewModel.selectedNotes.add(note)
                    notifyDataSetChanged()
                }
                onItemClick(notesList[position], open)
            }

        }
        holder.itemView.setOnLongClickListener {
            if (!States.noteEdited){
                val note = notesList[position]
                if (note in mainViewModel.selectedNotes) mainViewModel.selectedNotes.remove(note)
                else mainViewModel.selectedNotes.add(note)
                notifyDataSetChanged()
                onItemClick(note, false)

            }
            return@setOnLongClickListener true
        }
    }

    fun setData(notes: List<Note>? = null) {
        notesList = notes?.sortedBy{ it.time }?.reversed() ?: notesList
        notifyDataSetChanged()
    }
}