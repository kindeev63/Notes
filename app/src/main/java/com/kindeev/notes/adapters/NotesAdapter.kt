package com.kindeev.notes.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.other.NoteViewModel
import com.kindeev.notes.R
import com.kindeev.notes.other.States
import com.kindeev.notes.databinding.NoteItemBinding
import com.kindeev.notes.db.Note

class NotesAdapter(private val noteViewModel: NoteViewModel, private val onItemClick: (note: Note, open: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    private var notesList = emptyList<Note>()
    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note, choosingNotes: Boolean, noteSelected: Boolean) = with(binding) {
            tTitle.text = note.title
            tTime.text = note.time
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
        holder.bind(notesList[position], noteViewModel.selectedNotes.size > 0, notesList[position] in noteViewModel.selectedNotes)
        holder.itemView.setOnClickListener {
            if (!States.noteEdited) {
                val open = noteViewModel.selectedNotes.size == 0
                if (noteViewModel.selectedNotes.size > 0){
                    val note = notesList[position]
                    if (note in noteViewModel.selectedNotes) noteViewModel.selectedNotes.remove(note)
                    else noteViewModel.selectedNotes.add(note)
                    notifyDataSetChanged()
                }
                onItemClick(notesList[position], open)
            }

        }
        holder.itemView.setOnLongClickListener {
            if (!States.noteEdited){
                val note = notesList[position]
                if (note in noteViewModel.selectedNotes) noteViewModel.selectedNotes.remove(note)
                else noteViewModel.selectedNotes.add(note)
                notifyDataSetChanged()
                onItemClick(note, false)

            }
            return@setOnLongClickListener true
        }
    }

    fun setData(notes: List<Note>? = null) {
        notesList = notes ?: notesList
        notifyDataSetChanged()
    }
}