package com.kindeev.notes.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kindeev.notes.R
import com.kindeev.notes.databinding.NoteItemBinding
import com.kindeev.notes.db.Note
import com.kindeev.notes.other.Colors
import java.text.SimpleDateFormat
import java.util.*

class NotesAdapter(private val onItemClick: (note: Note, long: Boolean) -> Unit) :
    RecyclerView.Adapter<NotesAdapter.NotesHolder>() {
    private var notesList = emptyList<Note>()
    private var selectedNotesList = emptyList<Note>()

    class NotesHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = NoteItemBinding.bind(view)
        fun bind(note: Note, selectedNotes: List<Note>) = with(binding) {
            noteTitle.text = note.title
            noteTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(note.time)
            noteDate.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(note.time)
            noteColorView.setBackgroundColor(Colors.colors[note.colorIndex].primary)
            noteColorSeparatorView.setBackgroundColor(Colors.colors[note.colorIndex].secondary)
                if (note in selectedNotes) {
                    notePickingView.setBackgroundColor(Color.BLACK)
                } else {
                    notePickingView.setBackgroundColor(Color.TRANSPARENT)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = NotesHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
    )

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: NotesHolder, position: Int) {
        holder.bind(
            notesList[position],
            selectedNotesList
        )
        holder.itemView.setOnClickListener {
            onItemClick(notesList[position], false)
        }
        holder.itemView.setOnLongClickListener {
            onItemClick(notesList[position], true)
            return@setOnLongClickListener true
        }
    }

    fun setData(notes: List<Note>? = null, selectedNotes: List<Note>? = null) {
        notes?.let { notesList = it }
        selectedNotes?.let { selectedNotesList = it }
        notifyDataSetChanged()
    }
}